package com.xyx.java.parseITjuzi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
		// TODO parse IT桔子
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
		while (currentPageIndex <= pageCount) {
			Document document = PARSER.httpGet2Document(String.format(formatUrl, currentPageIndex));
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