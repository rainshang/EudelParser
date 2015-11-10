package com.xyx.java.parseITjuzi;

public class Project {
	public int projectId;
	public String projectName;
	public String projectBrief;
	public String projectCompany;
	public String projectIndustry;
	public String projectWebsite;
	public String projectLocation;

	public String projectFundPhase;

	public PastFinance[] pastFinances;
	public Person[] founders;

	public Project(int projectId, String projectName, String projectBrief, String projectCompany, String projectIndustry, String projectWebsite, String projectLocation, String projectFundPhase) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectBrief = projectBrief;
		this.projectCompany = projectCompany;
		this.projectIndustry = projectIndustry;
		this.projectWebsite = projectWebsite;
		this.projectLocation = projectLocation;
		this.projectFundPhase = projectFundPhase;
	}

	/**
	 * 融资经历
	 * 
	 * @author rainshang
	 * 
	 */
	public class PastFinance {
		public String date;
		public String phase;
		public String financeAmount;
		public String[] participants;

		public PastFinance(String date, String phase, String financeAmount, String[] participants) {
			this.date = date;
			this.phase = phase;
			this.financeAmount = financeAmount;
			this.participants = participants;
		}
	}

	/**
	 * 人员信息
	 * 
	 * @author rainshang
	 * 
	 */
	public class Person {
		public String name;
		public String type;

		public String allIntro;
		public String[] tags;

		public PersonCompanyExp[] companyExps;

		public Person(String name, String type, String allIntro, String[] tags) {
			this.name = name;
			this.type = type;
			this.allIntro = allIntro;
			this.tags = tags;
		}

		public class PersonCompanyExp {
			public String groupName;
			public String brief;
			public String positionString;
			public String date;

			public PersonCompanyExp(String groupName, String brief, String positionString, String date) {
				this.groupName = groupName;
				this.brief = brief;
				this.positionString = positionString;
				this.date = date;
			}

		}

	}

}