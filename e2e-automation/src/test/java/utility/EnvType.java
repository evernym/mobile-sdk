package test.java.utility;


public enum EnvType {
	QA("qa"),
	Demo("demo"),
	DevTeam1("devteam1");

	private String value;

	public String getValue() {
		return value;
	}

	EnvType(String value) {
		this.value = value;
	}
}