package com.xyx.java.parseITjuzi;

import java.text.SimpleDateFormat;

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

	public PastFinance[] pastFinances;
	public PastInvestor[] pastInvestors;
	public Person[] founders;
	public Person[] employees;

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

		public class PersonCompanyExp {
			public String groupName;
			public String brief;
			public String positionString;
			public long startDateL, endDateL;

		}

		public class PersonWorkExp {
			public String groupName;
			public String positionString;
			public String positionDetail;
			public long startDateL, endDateL;

		}
	}

}