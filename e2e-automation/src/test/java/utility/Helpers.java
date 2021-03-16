package test.java.utility;

import java.util.*;

public class Helpers {

	public static String randomString() {
		int leftLimit = 97;
		int rightLimit = 122;
		int targetStringLength = 10;
		Random random = new Random();

		return random.ints(leftLimit, rightLimit + 1)
				.limit(targetStringLength)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public static String UUID4() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static List<String> twoAttributes() {
		return Arrays.asList("Label", "Attachment_link");
	}

	public static List<String> fourAttributes() {
		return Arrays.asList("FirstName", "LastName", "Years", "Status");
	}

	public static List<String> nAttributes(int n) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < n; i++)
			list.add(randomString());
		return list;
	}

	public static List<String> allAttachmentsAttributes() {
//		return Arrays.asList("Label", "Photo_link", "PDF_link", "DOCX_link");
		return Arrays.asList("Photo_link", "PDF_link", "DOCX_link", "CSV_link");
	}
}
