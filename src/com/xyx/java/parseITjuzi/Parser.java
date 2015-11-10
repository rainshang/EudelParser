package com.xyx.java.parseITjuzi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.xyx.java.parseITjuzi.Project.PastFinance;
import com.xyx.java.parseITjuzi.Project.Person;
import com.xyx.java.parseITjuzi.Project.Person.PersonCompanyExp;

public class Parser {
	private final static String COOKIE = "BAIDU_DUP_lcr=https://www.baidu.com/link?url=SACPyz-DQJIByrfzGenEWE-6VOfkkye-PeE1oWWD9iK&wd=&eqid=cda04c5600002e6e00000003564003d3; gr_user_id=68d974ae-b3c0-4176-9623-76e253ba672d; cisession=a%3A4%3A%7Bs%3A10%3A%22session_id%22%3Bs%3A32%3A%222fbda95b63aaac150bcc7b5e47b01351%22%3Bs%3A10%3A%22ip_address%22%3Bs%3A14%3A%22123.115.64.135%22%3Bs%3A10%3A%22user_agent%22%3Bs%3A120%3A%22Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_11_1%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F46.0.2490.80+Safari%2F537.36%22%3Bs%3A13%3A%22last_activity%22%3Bi%3A1447052247%3B%7D82476f1c6fe3624b251bc054c4f8c0a8; front_identity=1252394237%40qq.com; front_remember_code=8b4c03831e15efc87c8ce255907b16d371dcda31; Hm_lvt_1c587ad486cdb6b962e94fc2002edf89=1447035866; Hm_lpvt_1c587ad486cdb6b962e94fc2002edf89=1447052359; gr_session_id=8bf6d5ca-9ffd-4759-88bc-0db9a314a993";

	private static Parser PARSER;

	private ArrayList<Project> projects;

	public static void parse() {
		if (PARSER == null) {
			PARSER = new Parser();
		} else {
			PARSER.projects.clear();
		}
		PARSER.parseITjuzi("http://itjuzi.com/company?scope=12&page=%1$d");
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
							person.companyExps[j] = person.new PersonCompanyExp(companyExpName, companyExpBrief, positionString, date);
						}
						project.founders[i] = person;
					}
				}
				projects.add(project);
				currentPageIndex++;
			}
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
}