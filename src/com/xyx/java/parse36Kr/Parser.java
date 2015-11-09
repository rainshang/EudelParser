package com.xyx.java.parse36Kr;

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

import org.json.JSONArray;
import org.json.JSONObject;

import com.xyx.java.EudelParser.EudelParser;
import com.xyx.java.parse36Kr.Project.Person;
import com.xyx.java.parse36Kr.Project.Person.PersonCompanyExp;

public class Parser {

	private final static String COOKIE = "kr_stat_uuid=JNJeN23979065; BAIDU_DUP_lcr=https://www.baidu.com/link?url=uye-tu0p0xHUJutxGBtMCOoIjFhZef-vp9w4tP38-ra&wd=&eqid=b3420d4400016eb800000003563abab4; passport_remember_user_token=W1syNTQzODRdLCIyWjlWV3pSbzhnWkRKZTdEYnpHWCJd--74f63e20fa57d0862ff499dfcba92affc555a5cc; kr_plus_id=106366; kr_plus_token=dc51cf5caf3bafff802e0480dec20dbdc31a2ea0; Hm_lvt_713123c60a0e86982326bae1a51083e1=1445242886,1445244146,1445928256,1446689464; Hm_lpvt_713123c60a0e86982326bae1a51083e1=1446703813; c_name=point; Hm_lvt_e8ec47088ed7458ec32cde3617b23ee3=1446689505; Hm_lpvt_e8ec47088ed7458ec32cde3617b23ee3=1446703823; krid_user_version=9; krid_user_id=254384; _passport_session=13dae99bdad62b6598829ec219adaec1; XSRF-TOKEN=eyJpdiI6IjNDdXdtWW04cG9QNWRcL2cwNUczbFd3PT0iLCJ2YWx1ZSI6IkdtNHU5OGNDc2lIVmE0OTRWSlBDOGs3UUtsMmJFdVNSc0lTNkdMTnBmc2ZIM0FkbzFvWitJeElSQk1yU2IzdDcxOHVHME5hOXgzWFRoQldkYWhaY1BBPT0iLCJtYWMiOiJkNjhlNzk5MjU1Y2Y5NjAwNjNiMDVmYWVjMjdlNGM4Y2E2NmQ5OTQwN2Q3OTdhODZjZjQxZDRlNGQ3Y2ZmMmVmIn0%3D; krchoasss=eyJpdiI6ImhmXC9wXC9Sa3oybURFSDJCSEUxQmJmUT09IiwidmFsdWUiOiJQOVp5dVlwUFlDSVpDSUExT0dEeVl6YVNNUzNaelRoY1Q2dmpkRnJ3UFVlN05Bd1lkMXc3dVY0UURRVmZcL2xiVzlOZmNCWW53RXkzNGgyZW9sV1ZtTFE9PSIsIm1hYyI6ImY5MmY0ZTFkYjdjYTdkZmFiMWIwYzM2MDAxNGVjOWE1N2UxYjFlMDZjM2I1YTg3YTg2MDU5ZTdhNjU0M2JjNWIifQ%3D%3D; _krypton_session=98186e45c407843f2f5b156a390f9775";
	private final static String[] CATEGORY = { "金融", "智能硬件", "电子商务", "企业服务" };
	private final static String[] FORMAT_URL = { "https://rong.36kr.com/api/company?fincestatus=0&industry=FINANCE&page=%1$d&type=", "https://rong.36kr.com/api/company?fincestatus=0&industry=INTELLIGENT_HARDWARE&page=1&type=", "https://rong.36kr.com/api/company?fincestatus=0&industry=E_COMMERCE&page=1&type=", "https://rong.36kr.com/api/company?fincestatus=0&industry=SERVICE_INDUSTRIES&page=1&type=" };

	private static Parser PARSER;

	private ArrayList<Project> projects;

	public static void parse() {
		if (PARSER == null) {
			PARSER = new Parser();
		} else {
			PARSER.projects.clear();
		}
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(new File("36Kr.xls"));
			for (int i = 0; i < CATEGORY.length; i++) {
				PARSER.projects.clear();
				System.out.println(CATEGORY[i]);
				System.out.println("开始抓取网页...");
				long during = System.currentTimeMillis();
				PARSER.parse36Kr(FORMAT_URL[i]);
				during -= System.currentTimeMillis();
				System.out.println(String.format("网页抓取结束，共抓取 %1$d条数据，耗时 %2$d秒", PARSER.projects.size(), -during / 1000));

				System.out.println("开始导出Excel...");
				during = System.currentTimeMillis();
				PARSER.saveAsExcel(workbook, CATEGORY[i], i);
				during -= System.currentTimeMillis();
				System.out.println(String.format("Excel导出结束，耗时 %1$d秒", -during / 1000));
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
		projects = new ArrayList<>(400);
	}

	private void parse36Kr(String formatUrl) {
		int currentPageIndex = 1;
		int pageCount = Integer.MAX_VALUE;
		while (currentPageIndex <= pageCount) {
			JSONObject onePageJsonObject = PARSER.httpGet2JSON(String.format(formatUrl, currentPageIndex));
			onePageJsonObject = onePageJsonObject.getJSONObject("data").getJSONObject("page");
			pageCount = onePageJsonObject.getInt("totalPages");
			JSONArray jsonArray = onePageJsonObject.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject projectJSON = jsonArray.getJSONObject(i);
				Project project = new Project(projectJSON);

				JSONObject pastFinanceJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/company/%1$d/past-finance", project.projectId));
				project.updatePastFinanceInfo(pastFinanceJson);

				JSONObject pastInvestorJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/company/%1$d/past-investor?pageSize=100", project.projectId));
				project.updatePastInvestorInfo(pastInvestorJson);

				JSONObject founderJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/company/%1$d/founder?pageSize=1000", project.projectId));
				project.updateFounderInfo(founderJson);
				for (Person founder : project.founders) {
					JSONObject basicJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/basic", founder.id));
					founder.updateBasicInfo(basicJson);

					JSONObject companyExpJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/company", founder.id));
					founder.updateCompanyExpInfo(companyExpJson);

					JSONObject workExpJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/work", founder.id));
					founder.updateWorkExpInfo(workExpJson);
				}

				JSONObject employeeJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/company/%1$d/employee?pageSize=1000", project.projectId));
				project.updateEmployeeInfo(employeeJson);
				for (Person employee : project.employees) {
					JSONObject basicJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/basic", employee.id));
					employee.updateBasicInfo(basicJson);

					JSONObject companyExpJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/company", employee.id));
					employee.updateCompanyExpInfo(companyExpJson);

					JSONObject workExpJson = PARSER.httpGet2JSON(String.format("https://rong.36kr.com/api/user/%1$d/work", employee.id));
					employee.updateWorkExpInfo(workExpJson);
				}

				PARSER.projects.add(project);
			}
			currentPageIndex++;
		}
	}

	private JSONObject httpGet2JSON(String getUrl) {
		JSONObject jsonObject = null;
		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(getUrl).openConnection();
			httpURLConnection.setRequestProperty("Cookie", COOKIE);
			httpURLConnection.connect();
			InputStream inputStream = httpURLConnection.getInputStream();
			jsonObject = new JSONObject(EudelParser.inputStream2String(inputStream));
			httpURLConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	private void saveAsExcel(WritableWorkbook workbook, String sheetName, int sheetIndex) throws WriteException {
		WritableSheet sheet = workbook.createSheet(sheetName, sheetIndex);
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
			label = new Label(2, projectInLine, project.getFundPhase());
			sheet.addCell(label);
			label = new Label(3, projectInLine, project.getFundValue());
			sheet.addCell(label);
			label = new Label(11, projectInLine, project.projectIndustry);
			sheet.addCell(label);
			label = new Label(12, projectInLine, project.getAddress());
			sheet.addCell(label);
			label = new Label(13, projectInLine, project.getPastFinances());
			sheet.addCell(label);
			label = new Label(14, projectInLine, project.getPastInvestors());
			sheet.addCell(label);
			label = new Label(15, projectInLine, project.projectStory);
			sheet.addCell(label);
			label = new Label(16, projectInLine, project.getEmployees());
			sheet.addCell(label);
			label = new Label(17, projectInLine, project.projectIntro);
			sheet.addCell(label);
			label = new Label(18, projectInLine, project.projectWebsite);
			sheet.addCell(label);

			int founderInLine = projectInLine;
			for (Person founder : project.founders) {
				label = new Label(4, founderInLine, founder.getDisplayName());
				sheet.addCell(label);
				Number number = new Number(9, founderInLine, founder.companyExps.length);
				sheet.addCell(number);
				label = new Label(10, founderInLine, founder.getWorkExps());
				sheet.addCell(label);

				int companyExpInLine = founderInLine;
				for (PersonCompanyExp companyExp : founder.companyExps) {
					label = new Label(5, companyExpInLine, companyExp.groupName + "，" + companyExp.brief);
					sheet.addCell(label);
					label = new Label(6, companyExpInLine, companyExp.positionString);
					sheet.addCell(label);
					label = new Label(7, companyExpInLine, project.getDate(companyExp.startDateL));
					sheet.addCell(label);
					label = new Label(8, companyExpInLine, project.getDate(companyExp.endDateL));
					sheet.addCell(label);
					companyExpInLine++;
				}
				if (companyExpInLine > founderInLine + 1) {
					sheet.mergeCells(4, founderInLine, 4, companyExpInLine - 1);
					sheet.mergeCells(9, founderInLine, 9, companyExpInLine - 1);
					sheet.mergeCells(10, founderInLine, 10, companyExpInLine - 1);
				}
				founderInLine = companyExpInLine;
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