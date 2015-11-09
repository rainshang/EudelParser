package com.xyx.java.parse36Kr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class Project {
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM");

	public int projectId;
	public String projectName;
	public String projectBrief;
	public String projectCompany;
	public String projectIndustry;
	public String projectIntro;
	public String projectStory;
	public String projectWebsite;
	public int[] projectAddressCodes;

	public String projectFundPhase;
	public String projectFundValue;
	public String projectFundUnit;

	public PastFinance[] pastFinances;// https://rong.36kr.com/api/company/141247/past-finance
	public PastInvestor[] pastInvestors;// https://rong.36kr.com/api/company/33698/past-investor?pageSize=100
	public Person[] founders;// https://rong.36kr.com/api/company/141247/founder?pageSize=1000
	public Person[] employees;// https://rong.36kr.com/api/company/141247/employee?pageSize=1000

	public Project(JSONObject projectJson) {
		JSONObject companyJson = projectJson.getJSONObject("company");
		projectId = companyJson.getInt("id");
		projectName = companyJson.getString("name");
		projectBrief = companyJson.getString("brief");
		projectCompany = companyJson.getString("fullName");
		projectIndustry = companyJson.getString("industry");
		projectIntro = companyJson.getString("intro");
		projectStory = companyJson.getString("story");
		projectWebsite = companyJson.getString("website");
		ArrayList<Integer> addressList = new ArrayList<>();
		for (int i = 1; i < 4; i++) {
			int addressCode = companyJson.optInt("address" + i, 0);
			if (addressCode != 0) {
				addressList.add(addressCode);
			}
		}
		projectAddressCodes = new int[addressList.size()];
		for (int i = 0; i < projectAddressCodes.length; i++) {
			projectAddressCodes[i] = addressList.get(i);
		}

		JSONObject financingJson = projectJson.optJSONObject("financing");
		if (financingJson != null) {
			projectFundPhase = financingJson.getString("phase");
			projectFundValue = financingJson.getString("fundValue");
			projectFundUnit = financingJson.getString("fundValueUnit");
		}
	}

	/**
	 * https://rong.36kr.com/api/company/141247/past-finance
	 * 
	 * @param pastFinanceJson
	 */
	public void updatePastFinanceInfo(JSONObject pastFinanceJson) {
		JSONArray jsonArray = pastFinanceJson.getJSONObject("data").getJSONArray("data");
		pastFinances = new PastFinance[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject financeJson = jsonArray.getJSONObject(i);
			pastFinances[i] = new PastFinance(financeJson);
		}
	}

	/**
	 * https://rong.36kr.com/api/company/33698/past-investor?pageSize=100
	 * 
	 * @param pastInvestorJson
	 */
	public void updatePastInvestorInfo(JSONObject pastInvestorJson) {
		JSONArray jsonArray = pastInvestorJson.getJSONObject("data").getJSONArray("data");
		pastInvestors = new PastInvestor[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject investorJson = jsonArray.getJSONObject(i);
			pastInvestors[i] = new PastInvestor(investorJson);
		}
	}

	/**
	 * https://rong.36kr.com/api/company/141247/founder?pageSize=1000
	 * 
	 * @param founderJson
	 */
	public void updateFounderInfo(JSONObject founderJson) {
		JSONArray jsonArray = founderJson.getJSONObject("data").getJSONArray("data");
		founders = new Person[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject personJson = jsonArray.getJSONObject(i);
			founders[i] = new Person(personJson);
		}
	}

	/**
	 * https://rong.36kr.com/api/company/141247/employee?pageSize=1000
	 * 
	 * @param employeeJson
	 */
	public void updateEmployeeInfo(JSONObject employeeJson) {
		JSONArray jsonArray = employeeJson.getJSONObject("data").getJSONArray("data");
		employees = new Person[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject personJson = jsonArray.getJSONObject(i);
			employees[i] = new Person(personJson);
		}
	}

	public String getFundPhase() {
		return projectFundPhase != null ? projectFundPhase : "未知";
	}

	public String getFundValue() {
		return projectFundValue != null ? projectFundValue + "万" + projectFundUnit : "未知";
	}

	public String getAddress() {
		return projectAddressCodes.length > 0 ? String.valueOf(projectAddressCodes[0]) : "未知";
	}

	public String getPastFinances() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < pastFinances.length; i++) {
			stringBuilder.append(pastFinances[i]);
			if (i < pastFinances.length - 1) {
				stringBuilder.append("\n\n");
			}
		}
		return stringBuilder.toString();
	}

	public String getPastInvestors() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < pastInvestors.length; i++) {
			stringBuilder.append(pastInvestors[i]);
			if (i < pastInvestors.length - 1) {
				stringBuilder.append("\n\n");
			}
		}
		return stringBuilder.toString();
	}

	public String getEmployees() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < employees.length; i++) {
			stringBuilder.append(employees[i].getDisplayName());
			stringBuilder.append("，");
			stringBuilder.append(employees[i].type);
			stringBuilder.append("，");
			stringBuilder.append(employees[i].intro);
			if (i < employees.length - 1) {
				stringBuilder.append("\n\n");
			}
		}
		return stringBuilder.toString();
	}

	private String getDate(long startDateL, long endDateL) {
		StringBuilder stringBuilder = new StringBuilder();
		if (startDateL != 0 && endDateL != 0) {
			stringBuilder.append(getDate(startDateL));
			stringBuilder.append(" － ");
			stringBuilder.append(getDate(endDateL));
		}
		return stringBuilder.toString();
	}

	public String getDate(long dateL) {
		if (dateL != 0 && dateL / System.currentTimeMillis() < 10) {
			return DATE_FORMAT.format(new Date(dateL));
		} else {
			return "  ";
		}
	}

	/**
	 * 融资经历
	 * 
	 * @author rainshang
	 * 
	 */
	public class PastFinance {
		public long dateL;
		public String phase;
		public String financeAmount;
		public String financeAmountUnit;
		public String valuation;
		public String valuationUnit;
		public String[] participants;

		public PastFinance(JSONObject financeJson) {
			dateL = financeJson.getLong("financeDate");
			phase = financeJson.getString("phase");
			financeAmount = financeJson.optString("financeAmount");
			financeAmountUnit = financeJson.optString("financeAmountUnit");
			valuation = financeJson.optString("valuation");
			valuationUnit = financeJson.optString("valuationUnit");
			JSONArray jsonArray = financeJson.optJSONArray("participants");
			if (jsonArray != null) {
				participants = new String[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					participants[i] = jsonArray.getJSONObject(i).getString("name");
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(phase);
			stringBuilder.append("，");
			stringBuilder.append(getDate(dateL));
			if (financeAmount != null) {
				stringBuilder.append("，融资金额：");
				stringBuilder.append(financeAmount);
				stringBuilder.append(financeAmountUnit);
			}
			if (valuation != null) {
				stringBuilder.append("，融资估值：");
				stringBuilder.append(valuation);
				stringBuilder.append(valuationUnit);
			}
			String participant = getParticipants();
			if (participant.length() > 0) {
				stringBuilder.append("，融资方：");
				stringBuilder.append(participant);
			}
			return stringBuilder.toString();
		}

		private String getParticipants() {
			StringBuilder stringBuilder = new StringBuilder();
			if (participants != null) {
				for (int i = 0; i < participants.length; i++) {
					stringBuilder.append(participants[i]);
					if (i < participants.length - 1) {
						stringBuilder.append("、");
					}
				}
			}
			return stringBuilder.toString();
		}
	}

	/**
	 * 过往资方
	 * 
	 * @author rainshang
	 * 
	 */
	public class PastInvestor {
		public String name;
		public String brief;

		public PastInvestor(JSONObject investorJson) {
			name = investorJson.getString("name");
			brief = investorJson.optString("brief");
		}

		@Override
		public String toString() {
			return brief != null ? name + '（' + brief + '）' : name;
		}
	}

	/**
	 * 人员信息
	 * 
	 * @author rainshang
	 * 
	 */
	public class Person {
		public int id;
		public String name;
		public String nickName;
		public String type;
		public String intro;

		// https://rong.36kr.com/api/user/53366/basic
		public String allIntro;
		public String[] tags;

		public PersonCompanyExp[] companyExps;// https://rong.36kr.com/api/user/53366/company
		public PersonWorkExp[] workExps;// https://rong.36kr.com/api/user/53366/work

		public Person(JSONObject personJson) {
			id = personJson.getInt("id");
			name = personJson.optString("name");
			nickName = personJson.optString("nickName");
			type = personJson.getString("type");
			intro = personJson.optString("intro");
		}

		/**
		 * https://rong.36kr.com/api/user/53366/basic
		 * 
		 * @param basicJson
		 */
		public void updateBasicInfo(JSONObject basicJson) {
			basicJson = basicJson.getJSONObject("data");
			allIntro = basicJson.optString("intro");
			JSONArray jsonArray = basicJson.optJSONArray("industry");
			if (jsonArray != null) {
				tags = new String[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					tags[i] = jsonArray.getString(i);
				}
			}
		}

		/**
		 * https://rong.36kr.com/api/user/53366/company
		 * 
		 * @param companyExpJson
		 */
		public void updateCompanyExpInfo(JSONObject companyExpJson) {
			JSONArray jsonArray = companyExpJson.getJSONObject("data").getJSONArray("expList");
			companyExps = new PersonCompanyExp[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject expJson = jsonArray.getJSONObject(i);
				companyExps[i] = new PersonCompanyExp(expJson);
			}
		}

		/**
		 * https://rong.36kr.com/api/user/53366/work
		 * 
		 * @param workExpJson
		 */
		public void updateWorkExpInfo(JSONObject workExpJson) {
			JSONArray jsonArray = workExpJson.getJSONObject("data").getJSONArray("expList");
			workExps = new PersonWorkExp[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject expJson = jsonArray.getJSONObject(i);
				workExps[i] = new PersonWorkExp(expJson);
			}
		}

		public String getDisplayName() {
			if (name != null) {
				return name;
			} else {
				return nickName;
			}
		}

		public String getCompanyExps() {
			StringBuilder stringBuilder = new StringBuilder();
			for (PersonCompanyExp iterable_element : companyExps) {
				stringBuilder.append(iterable_element.groupName);
				stringBuilder.append("，");
				stringBuilder.append(iterable_element.brief);
				stringBuilder.append("，");
				stringBuilder.append(iterable_element.positionString);
				if (iterable_element.startDateL != 0 && iterable_element.endDateL != 0) {
					stringBuilder.append("，");
					stringBuilder.append(getDate(iterable_element.startDateL, iterable_element.endDateL));
				}
				stringBuilder.append("\n\n");
			}
			return stringBuilder.toString();
		}

		public String getWorkExps() {
			StringBuilder stringBuilder = new StringBuilder();
			for (PersonWorkExp iterable_element : workExps) {
				stringBuilder.append(iterable_element.groupName);
				stringBuilder.append(iterable_element.positionString);
				if (iterable_element.positionDetail != null) {
					stringBuilder.append("，");
					stringBuilder.append(iterable_element.positionDetail);
				}
				if (iterable_element.startDateL != 0 && iterable_element.endDateL != 0) {
					stringBuilder.append("，");
					stringBuilder.append(getDate(iterable_element.startDateL, iterable_element.endDateL));
				}
				stringBuilder.append("\n\n");
			}
			return stringBuilder.toString();
		}

		public class PersonCompanyExp {
			public String groupName;
			public String brief;
			public String positionString;
			public long startDateL, endDateL;

			public PersonCompanyExp(JSONObject companyExpJson) {
				groupName = companyExpJson.getString("groupName");
				brief = companyExpJson.getString("brief");
				positionString = companyExpJson.getString("positionString");
				startDateL = companyExpJson.optLong("startDate");
				endDateL = companyExpJson.optLong("endDate");
			}
		}

		public class PersonWorkExp {
			public String groupName;
			public String positionString;
			public String positionDetail;
			public long startDateL, endDateL;

			public PersonWorkExp(JSONObject workExpJson) {
				groupName = workExpJson.getString("groupName");
				positionString = workExpJson.getString("positionString");
				positionDetail = workExpJson.optString("positionDetail");
				startDateL = workExpJson.optLong("startDate");
				endDateL = workExpJson.optLong("endDate");
			}
		}
	}

}