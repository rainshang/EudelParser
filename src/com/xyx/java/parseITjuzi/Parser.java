package com.xyx.java.parseITjuzi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xyx.java.parseITjuzi.Project.PastFinance;
import com.xyx.java.parseITjuzi.Project.Person;
import com.xyx.java.parseITjuzi.Project.Person.PersonCompanyExp;

public class Parser {
	private final static String COOKIE = "BAIDU_DUP_lcr=https://www.baidu.com/link?url=SACPyz-DQJIByrfzGenEWE-6VOfkkye-PeE1oWWD9iK&wd=&eqid=cda04c5600002e6e00000003564003d3; gr_user_id=68d974ae-b3c0-4176-9623-76e253ba672d; cisession=a%3A4%3A%7Bs%3A10%3A%22session_id%22%3Bs%3A32%3A%222fbda95b63aaac150bcc7b5e47b01351%22%3Bs%3A10%3A%22ip_address%22%3Bs%3A14%3A%22123.115.64.135%22%3Bs%3A10%3A%22user_agent%22%3Bs%3A120%3A%22Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_11_1%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F46.0.2490.80+Safari%2F537.36%22%3Bs%3A13%3A%22last_activity%22%3Bi%3A1447052247%3B%7D82476f1c6fe3624b251bc054c4f8c0a8; front_identity=1252394237%40qq.com; front_remember_code=8b4c03831e15efc87c8ce255907b16d371dcda31; Hm_lvt_1c587ad486cdb6b962e94fc2002edf89=1447035866; Hm_lpvt_1c587ad486cdb6b962e94fc2002edf89=1447052359; gr_session_id=8bf6d5ca-9ffd-4759-88bc-0db9a314a993";
	private final static String[] FORMAT_URL = { "http://itjuzi.com/company?scope=12&page=%1$d" };

	private static Parser PARSER;

	private ArrayList<Project> projects;

	public static void parse() {
		if (PARSER == null) {
			PARSER = new Parser();
		} else {
			PARSER.projects.clear();
		}
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(new File("ITjuzi.xls"));
			for (int i = 0; i < FORMAT_URL.length; i++) {
				PARSER.projects.clear();
				System.out.println("开始抓取网页...");
				long during = System.currentTimeMillis();
				PARSER.parseITjuzi(FORMAT_URL[i]);
				during -= System.currentTimeMillis();
				System.out.println(String.format("网页抓取结束，共抓取 %1$d条数据，耗时 %2$d秒", PARSER.projects.size(), -during / 1000));

				System.out.println("开始导出Excel...");
				during = System.currentTimeMillis();
				PARSER.saveAsExcel(workbook, i);
				during -= System.currentTimeMillis();
				System.out.println(String.format("%1$s 分类的Excel导出结束，耗时 %2$d秒", PARSER.projects.get(0).projectIndustry, -during / 1000));
				System.out.println("\n");
			}
			workbook.write();
			workbook.close();
			System.out.println("Done");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}

	public Parser() {
		projects = new ArrayList<>();
	}

	/**
	 * @param formatUrl
	 *            http://itjuzi.com/company?scope=12&page=1
	 */
	private void parseITjuzi(String formatUrl) {
		int currentPageIndex = 1;
		int pageCount = Integer.MAX_VALUE;
		String industry = null;
		while (currentPageIndex <= pageCount) {
			Document document = PARSER.httpGet2Document(String.format(formatUrl, currentPageIndex));
			Element element = document.select("div[class=\"normal-box-no-pad follow-area\"]").get(0);
			if (industry == null) {
				industry = element.child(0).select("li[class=\"active\"]").get(0).text();
			}
			if (pageCount == Integer.MAX_VALUE) {
				pageCount = Integer.parseInt(element.child(2).select("li[class=\"next page\"]").get(1).child(0).attr("href").split("page=")[1]);
			}
			Elements companyElements = element.select("div[class=\"company-list-item \"]");
			for (Element companyElement : companyElements) {
				String name = companyElement.child(0).child(0).child(0).text();
				System.out.println(name);
				// String location = companyElement.child(0).child(1).child(0).text();
				// String brief = companyElement.child(1).text();
				String idUrl = companyElement.child(2).attr("href");

				document = PARSER.httpGet2Document(idUrl);
				// element = document.select("div[class=\"normal-box clearfix\"]").get(0);
				// element = element.child(1);
				// String website = element.child(0).child(1).text();
				// String company = element.child(1).text();
				element = document.select("section[id=\"page-content\"]").get(0);
				Element leftElement = element.child(0);
				Elements detailElements = leftElement.select("ul[class=\"detail-info\"]").get(0).children();

				String website = null;
				String company = null;
				String date = null;
				String location = null;
				String brief = null;
				for (Element detailElement : detailElements) {
					String[] titleContent = detailElement.text().split(":");
					switch (titleContent[0]) {
					case "网址":
						if (titleContent.length == 3) {
							website = titleContent[1] + titleContent[2].trim();
						}
						break;

					case "公司":
						company = titleContent[1].trim();
						break;

					case "时间":
						date = titleContent[1].trim();
						break;
					case "地点":
						location = titleContent[1].trim();
						break;
					case "简介":
						brief = titleContent[1].trim();
						break;
					}
				}
				Element rightElement = element.child(1);
				String phase = rightElement.select("p[id=\"company-fund-status\"]").get(0).text();
				String[] ss = idUrl.split("/");
				Project project = new Project(Integer.parseInt(ss[ss.length - 1]), name, brief, company, industry, website, location, phase);
				Elements fundElements = rightElement.child(1).select("div[class=\"company-fund-item\"]");
				if (fundElements != null && fundElements.size() > 0) {
					project.pastFinances = new PastFinance[fundElements.size()];
					for (int i = 0; i < project.pastFinances.length; i++) {
						Element fundElement = fundElements.get(i);
						String fundDate = fundElement.child(0).text();
						String fundPhase = fundElement.child(0).child(0).text();
						String financeAmount = fundElement.child(1).text();
						String[] participants = null;
						if (fundElement.childNodeSize() > 2) {
							Elements participantElements = fundElement.child(2).children();
							participants = new String[participantElements.size()];
							for (int j = 0; j < participants.length; j++) {
								participants[j] = participantElements.get(j).text();
							}
						}
						project.pastFinances[i] = project.new PastFinance(fundDate, fundPhase, financeAmount, participants);
					}
				}

				Elements founderElements = leftElement.select("table[id=\"company-member-list-tbl\"]");
				if (founderElements != null && founderElements.size() > 0) {
					founderElements = founderElements.get(0).child(0).children();
					project.founders = new Person[founderElements.size()];
					for (int i = 0; i < project.founders.length; i++) {
						Element founderElement = founderElements.get(i);
						String founderUrl = founderElement.child(1).child(0).attr("href");
						String founderName = founderElement.child(1).text();
						String founderType = founderElement.child(2).text();

						document = httpGet2Document(founderUrl);
						element = document.select("article[class=\"two-col-big-left\"]").get(0);
						Elements founderAllIntroElements = element.child(0).child(0).child(1).child(0).children();
						String founderAllIntro = founderAllIntroElements.get(founderAllIntroElements.size() - 1).child(0).text();
						Elements tagElements = element.child(1).child(1).child(1).children();
						String[] tags = new String[tagElements.size()];
						for (int j = 0; j < tags.length; j++) {
							tags[j] = tagElements.get(j).text();
						}
						Person person = project.new Person(founderName, founderType, founderAllIntro, tags);
						Elements companyExpElements = element.select("div[id=\"company-similar\"]").get(0).children();
						person.companyExps = new PersonCompanyExp[companyExpElements.size()];
						for (int j = 0; j < person.companyExps.length; j++) {
							Element companyExpElement = companyExpElements.get(j);
							String companyExpName = companyExpElement.child(1).child(0).child(0).text();
							String companyExpBrief = companyExpElement.child(1).child(1).text();
							String[] sss = companyExpElement.child(1).child(0).text().split(" ");
							String positionString = sss[sss.length - 1];
							String companyExpUrl = companyExpElement.child(1).child(0).child(0).attr("href");
							String companyExpDate = "";
							for (Element dateElement : httpGet2Document(companyExpUrl).select("ul[class=\"detail-info\"]").get(0).children()) {
								String[] titleContent = dateElement.text().split(":");
								if ("时间".equals(titleContent[0])) {
									companyExpDate = titleContent[1];
								}
							}
							person.companyExps[j] = person.new PersonCompanyExp(companyExpName, companyExpBrief, positionString, companyExpDate);
						}
						project.founders[i] = person;
					}
				}
				projects.add(project);
				// if (projects.size() == 10) {
				// break;
				// }
			}
			// if (projects.size() == 10) {
			// break;
			// }
			currentPageIndex++;
		}
	}

	private Document httpGet2Document(String getUrl) {
		Document document = null;
		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(getUrl).openConnection();
			httpURLConnection.setRequestProperty("Cookie", COOKIE);
			httpURLConnection.connect();
			InputStream inputStream = httpURLConnection.getInputStream();
			document = Jsoup.parse(inputStream, "UTF-8", "");
			inputStream.close();
			httpURLConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document;
	}

	private void saveAsExcel(WritableWorkbook workbook, int sheetIndex) throws WriteException {
		WritableSheet sheet = workbook.createSheet(projects.get(0).projectIndustry, sheetIndex);
		Label label = new Label(0, 0, "项目Id");
		sheet.addCell(label);
		sheet.mergeCells(0, 0, 0, 1);
		label = new Label(1, 0, "公司");
		sheet.addCell(label);
		sheet.mergeCells(1, 0, 1, 1);
		label = new Label(2, 0, "融资阶段");
		sheet.addCell(label);
		sheet.mergeCells(2, 0, 2, 1);
		label = new Label(3, 0, "融资金额");
		sheet.addCell(label);
		sheet.mergeCells(3, 0, 3, 1);
		label = new Label(4, 0, "创始人");
		sheet.addCell(label);
		sheet.mergeCells(4, 0, 4, 1);
		label = new Label(5, 0, "创业经历");
		sheet.addCell(label);
		sheet.mergeCells(5, 0, 8, 0);
		label = new Label(5, 1, "项目名");
		sheet.addCell(label);
		label = new Label(6, 1, "职位");
		sheet.addCell(label);
		label = new Label(7, 1, "起始时间");
		sheet.addCell(label);
		label = new Label(8, 1, "结束时间");
		sheet.addCell(label);
		label = new Label(9, 0, "创业次数");
		sheet.addCell(label);
		sheet.mergeCells(9, 0, 9, 1);
		label = new Label(10, 0, "工作经历");
		sheet.addCell(label);
		sheet.mergeCells(10, 0, 10, 1);
		label = new Label(11, 0, "行业");
		sheet.addCell(label);
		sheet.mergeCells(11, 0, 11, 1);
		label = new Label(12, 0, "所在地");
		sheet.addCell(label);
		sheet.mergeCells(12, 0, 12, 1);
		label = new Label(13, 0, "融资经历");
		sheet.addCell(label);
		sheet.mergeCells(13, 0, 13, 1);
		label = new Label(14, 0, "过往投资方");
		sheet.addCell(label);
		sheet.mergeCells(14, 0, 14, 1);
		label = new Label(15, 0, "团队介绍");
		sheet.addCell(label);
		sheet.mergeCells(15, 0, 15, 1);
		label = new Label(16, 0, "团队成员");
		sheet.addCell(label);
		sheet.mergeCells(16, 0, 16, 1);
		label = new Label(17, 0, "运作模式");
		sheet.addCell(label);
		sheet.mergeCells(17, 0, 17, 1);
		label = new Label(18, 0, "相关链接");
		sheet.addCell(label);
		sheet.mergeCells(18, 0, 18, 1);

		int projectInLine = 2;
		for (Project project : projects) {
			Number projectId = new Number(0, projectInLine, project.projectId);
			sheet.addCell(projectId);
			label = new Label(1, projectInLine, project.projectName);
			sheet.addCell(label);
			label = new Label(2, projectInLine, project.projectFundPhase);
			sheet.addCell(label);
			// label = new Label(3, projectInLine, pr);
			// sheet.addCell(label);
			label = new Label(11, projectInLine, project.projectIndustry);
			sheet.addCell(label);
			label = new Label(12, projectInLine, project.projectLocation);
			sheet.addCell(label);
			// label = new Label(13, projectInLine, project.getPastFinances());
			// sheet.addCell(label);
			// label = new Label(14, projectInLine, project.getPastInvestors());
			// sheet.addCell(label);
			label = new Label(15, projectInLine, project.projectBrief);
			sheet.addCell(label);
			// label = new Label(16, projectInLine, project.getEmployees());
			// sheet.addCell(label);
			// label = new Label(17, projectInLine, project.projectIntro);
			// sheet.addCell(label);
			label = new Label(18, projectInLine, project.projectWebsite);
			sheet.addCell(label);

			int founderInLine = projectInLine;
			if (project.founders != null) {
				for (Person founder : project.founders) {
					label = new Label(4, founderInLine, founder.name);
					sheet.addCell(label);
					Number number = new Number(9, founderInLine, founder.companyExps.length);
					sheet.addCell(number);
					// label = new Label(10, founderInLine, founder.getWorkExps());
					// sheet.addCell(label);

					int companyExpInLine = founderInLine;
					for (PersonCompanyExp companyExp : founder.companyExps) {
						label = new Label(5, companyExpInLine, companyExp.groupName + "，" + companyExp.brief);
						sheet.addCell(label);
						label = new Label(6, companyExpInLine, companyExp.positionString);
						sheet.addCell(label);
						label = new Label(7, companyExpInLine, companyExp.date);
						sheet.addCell(label);
						// label = new Label(8, companyExpInLine, project.getDate(companyExp.endDateL));
						// sheet.addCell(label);
						companyExpInLine++;
					}
					if (companyExpInLine > founderInLine + 1) {
						sheet.mergeCells(4, founderInLine, 4, companyExpInLine - 1);
						sheet.mergeCells(9, founderInLine, 9, companyExpInLine - 1);
						sheet.mergeCells(10, founderInLine, 10, companyExpInLine - 1);
					}
					founderInLine = companyExpInLine;
				}
			} else {
				founderInLine++;
			}

			if (founderInLine > projectInLine + 1) {
				sheet.mergeCells(0, projectInLine, 0, founderInLine - 1);
				sheet.mergeCells(1, projectInLine, 1, founderInLine - 1);
				sheet.mergeCells(2, projectInLine, 2, founderInLine - 1);
				sheet.mergeCells(3, projectInLine, 3, founderInLine - 1);
				sheet.mergeCells(11, projectInLine, 11, founderInLine - 1);
				sheet.mergeCells(12, projectInLine, 12, founderInLine - 1);
				sheet.mergeCells(13, projectInLine, 13, founderInLine - 1);
				sheet.mergeCells(14, projectInLine, 14, founderInLine - 1);
				sheet.mergeCells(15, projectInLine, 15, founderInLine - 1);
				sheet.mergeCells(16, projectInLine, 16, founderInLine - 1);
				sheet.mergeCells(17, projectInLine, 17, founderInLine - 1);
				sheet.mergeCells(18, projectInLine, 18, founderInLine - 1);
			}
			projectInLine = founderInLine;
		}
	}

}