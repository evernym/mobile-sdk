package test.java.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

public class CustomTestNGReporter implements IReporter {
	DecimalFormat formatDecimal = new DecimalFormat("##.00");
	// This is the customize emailabel report template file path.
	private static final String emailableReportTemplateFile = System.getProperty("user.dir")
			+ "/customize-emailable-report-template.html";

	static String suiteType;

	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {

		try {
			// Get content data in TestNG report template file.
			String customReportTemplateStr = this.readEmailabelReportTemplate();
			// Create custom report title.
			String customReportTitle = this.getCustomReportTitle("Custom TestNG Report");

			// Create test suite summary data.
			String customSuiteSummary = this.getTestSuiteSummary(suites);

			// Create test methods summary data.
			String customTestMethodSummary = this.getTestMehodSummary(suites);

			// Replace report title place holder with custom title.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$TestNG_Custom_Report_Title\\$",
					customReportTitle);

			// Replace test suite place holder with custom test suite summary.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$Test_Case_Summary\\$", customSuiteSummary);

			// Replace test methods place holder with custom test method
			// summary.
			customReportTemplateStr = customReportTemplateStr.replaceAll("\\$Test_Case_Detail\\$",
					customTestMethodSummary);

			// Write replaced test report content to
			// custom-emailable-report.html.
			File targetFile = new File(outputDirectory + "/custom-emailable-report.html");
			FileWriter fw = new FileWriter(targetFile);
			fw.write(customReportTemplateStr);
			fw.flush();
			fw.close();
			SendEmail.sendMail(Config.EmailList, customReportTemplateStr);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* Read template content. */
	private String readEmailabelReportTemplate() {
		StringBuffer retBuf = new StringBuffer();

		try {

			File file = new File(this.emailableReportTemplateFile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			while (line != null) {
				retBuf.append(line);
				line = br.readLine();
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} finally {
			return retBuf.toString();
		}
	}

	/* Build custom report title. */
	private String getCustomReportTitle(String title) {
		StringBuffer retBuf = new StringBuffer();
		retBuf.append(title + " " + this.getDateInStringFormat(new Date()));
		return retBuf.toString();
	}

	/* Build test suite summary data. */
	private String getTestSuiteSummary(List<ISuite> suites) {
		StringBuffer retBuf = new StringBuffer();

		try {
			int totalTestCount = 0;
			int totalTestPassed = 0;
			int totalTestFailed = 0;
			int totalTestSkipped = 0;

			for (ISuite tempSuite : suites) {
				// retBuf.append("<tr><td colspan=11><center><b>" +
				// tempSuite.getName() + "</b></center></td></tr>");

				Map<String, ISuiteResult> testResults = tempSuite.getResults();

				for (ISuiteResult result : testResults.values()) {
					retBuf.append("<tr>");
					ITestContext testObj = result.getTestContext();
					totalTestPassed = testObj.getPassedTests().getAllMethods().size();
					totalTestSkipped = testObj.getSkippedTests().getAllMethods().size();
					totalTestFailed = testObj.getFailedTests().getAllMethods().size();
					suiteType = testObj.getName();
					totalTestCount = totalTestPassed + totalTestSkipped + totalTestFailed;
					/* Test name. */
					retBuf.append("<td>");
					retBuf.append(suiteType);
					retBuf.append("</td>");

					/* Total method count. */
					retBuf.append("<td>");
					retBuf.append(totalTestCount);
					retBuf.append("</td>");

					/* Passed method count. */
					retBuf.append("<td bgcolor=#2DF58E>");
					retBuf.append(totalTestPassed);
					retBuf.append("</td>");

					/* Skipped method count. */
					retBuf.append("<td bgcolor=#FFFB08>");
					retBuf.append(totalTestSkipped);
					retBuf.append("</td>");

					/* Failed method count. */
					retBuf.append("<td bgcolor=#FA8072>");
					retBuf.append(totalTestFailed);
					retBuf.append("</td>");

					/* Append ENV type. */
					retBuf.append("<td>");
					retBuf.append(Config.Env_Type);
					retBuf.append("</td>");

					/* Append Release No. */
					retBuf.append("<td>");
					retBuf.append(Config.BuildNo);
					retBuf.append("</td>");

					/* Start Date */
					Date startDate = testObj.getStartDate();

					/* End Date */
					Date endDate = testObj.getEndDate();

					/* Execute Time */
				
					double deltaTime = endDate.getTime() - startDate.getTime(); String
				    deltaTimeStr = this.convertDeltaTimeToString(deltaTime);
				    retBuf.append("<td>"); 
				    retBuf.append(deltaTimeStr); 
				    retBuf.append("</td>");

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return retBuf.toString();
		}
	}

	/* Get date string format value. */
	private String getDateInStringFormat(Date date) {
		StringBuffer retBuf = new StringBuffer();
		if (date == null) {
			date = new Date();
		}
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		retBuf.append(df.format(date));
		return retBuf.toString();
	}

	/* Convert long type deltaTime to format hh:mm:ss:mi. */
	private String convertDeltaTimeToString(double deltaTime) {
		StringBuffer retBuf = new StringBuffer();

		double milli = deltaTime;

		double seconds = deltaTime / 1000;

		double minutes = seconds / 60;

		double hours = minutes / 60;
		formatDecimal.setRoundingMode(RoundingMode.UP);
		retBuf.append(formatDecimal.format(seconds));

		return retBuf.toString();
	}

	/* Get test method summary info. */
	private String getTestMehodSummary(List<ISuite> suites) {
		StringBuffer retBuf = new StringBuffer();

		try {
			for (ISuite tempSuite : suites) {
				// retBuf.append("<tr><td colspan=7><center><b>" +
				// tempSuite.getName() + "</b></center></td></tr>");

				Map<String, ISuiteResult> testResults = tempSuite.getResults();

				for (ISuiteResult result : testResults.values()) {

					ITestContext testObj = result.getTestContext();

					String testName = testObj.getName();
					/* Get passed test method related data. */
					IResultMap testPassedResult = testObj.getPassedTests();
					String passedTestMethodInfo = this.getTestMethodReport(testName, testPassedResult, true, false);
					retBuf.append(passedTestMethodInfo);

					/* Get failed test method related data. */
					IResultMap testFailedResult = testObj.getFailedTests();
					String failedTestMethodInfo = this.getTestMethodReport(testName, testFailedResult, false, false);
					retBuf.append(failedTestMethodInfo);

					/* Get skipped test method related data. */
					IResultMap testSkippedResult = testObj.getSkippedTests();
					String skippedTestMethodInfo = this.getTestMethodReport(testName, testSkippedResult, false, true);
					retBuf.append(skippedTestMethodInfo);

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return retBuf.toString();
		}
	}

	/* Get failed, passed or skipped test methods report. */
	private String getTestMethodReport(String testName, IResultMap testResultMap, boolean passedResult,
			boolean skippedResult) {
		StringBuffer retStrBuf = new StringBuffer();

		String resultTitle = testName;

		String color = "#FFFB08";

		if (skippedResult) {
			resultTitle = "SKIP";
			color = "#FFFB08";
		} else {
			if (!passedResult) {
				resultTitle = "FAIL";
				color = "#FA8072";
			} else {
				resultTitle = "PASS";
				color = "#2DF58E";
			}
		}

		Set<ITestResult> testResultSet = testResultMap.getAllResults();

		for (ITestResult testResult : testResultSet) {
			String testClassName = "";
			String testMethodName = "";
			String startDateStr = "";
			String executeTimeStr = "";
			String paramStr = "";
			String reporterMessage = "";
			String exceptionMessage = "";

			// Get testMethodName
			testMethodName = testResult.getMethod().getMethodName();

			// Get startDateStr
			long startTimeMillis = testResult.getStartMillis();
			startDateStr = this.getDateInStringFormat(new Date(startTimeMillis));

			// Get Execute time.
			double deltaMillis = testResult.getEndMillis() - testResult.getStartMillis();
			executeTimeStr = this.convertDeltaTimeToString(deltaMillis);

			// Get parameter list.
			Object paramObjArr[] = testResult.getParameters();
			for (Object paramObj : paramObjArr) {
				paramStr += (String) paramObj;
				paramStr += " ";
			}

			// Get reporter message list.
			List<String> repoterMessageList = Reporter.getOutput(testResult);
			for (String tmpMsg : repoterMessageList) {
				reporterMessage += tmpMsg;
				reporterMessage += " ";
			}

			// Get exception message.
			Throwable exception = testResult.getThrowable();
			if (exception != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				exception.printStackTrace(pw);
				exceptionMessage = sw.toString();
			}

			retStrBuf.append("<tr bgcolor=" + color + ">");
			/* Add test method name. */
			retStrBuf.append("<td>");
			retStrBuf.append(testMethodName);
			retStrBuf.append("</td>");

			/* Add execution time. */
			retStrBuf.append("<td>");
			retStrBuf.append(executeTimeStr);
			retStrBuf.append("</td>");

			/* Add element not found exception */
			retStrBuf.append("<td>");
			retStrBuf.append(reporterMessage);
			retStrBuf.append("</td>");
			try {
			/* Add java exception message. */
			retStrBuf.append("<td>");
			retStrBuf.append(exceptionMessage.substring(0, 300)+"...");
			retStrBuf.append("</td>");
			}
			catch(Exception e)
			{
			
			}
		}

		return retStrBuf.toString();
	}

	/* Convert a string array elements to a string. */
	private String stringArrayToString(String strArr[]) {
		StringBuffer retStrBuf = new StringBuffer();
		if (strArr != null) {
			for (String str : strArr) {
				retStrBuf.append(str);
				retStrBuf.append(" ");
			}
		}
		return retStrBuf.toString();
	}

	public String getGroup(ITestAnnotation annotation) {
		System.out.println(annotation.getGroups());
		return (annotation.getGroups().toString());

	}

}