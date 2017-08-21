package beanConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class Converter {

	private static Set<String> set = new HashSet<>();

	public static void main(String[] args) throws IOException {
		System.out.println("Target directory is: " + args[0]);
		File outputDir = new File(args[0].concat("/output_folder"));
		outputDir.mkdir();
		Files.walk(Paths.get(args[0]))
				.filter(path -> !path.toFile().getPath().toString()
						.contains("output_folder")
						&& path.getFileName().toString().contains(".xml"))
				.forEach(path -> modifier(path, outputDir));
		if (!set.isEmpty()) {
			System.out
					.println("Could not find these parameters (Check their existence in com.ericsson.msran.msr.interfaces): "
							+ set.toString());
		}
		format(outputDir.getPath());
	}

	public static void modifier(Path path, File outputDir) {
		System.out.println("\n==========" + path.getFileName().toString()
				+ ": Start!==========");
		Document output = DocumentHelper.createDocument();
		SAXReader reader = new SAXReader();
		try {
			Document document = (Document) reader.read(path.toFile());
			Element iBeans = document.getRootElement();
			Namespace np = iBeans.getNamespace();
			Element oBeans = output.addElement("beans");
			QName qName = new QName("beans", np);
			oBeans.setQName(qName);
			oBeans.addAttribute("xmlns:context",
					"http://www.springframework.org/schema/context")
					.addAttribute("xmlns:p",
							"http://www.springframework.org/schema/p")
					.addAttribute("xmlns:xsi",
							"http://www.w3.org/2001/XMLSchema-instance")
					.addAttribute(
							"xsi:schemaLocation",
							"http://www.springframework.org/schema/beans\t      http://www.springframework.org/schema/beans/spring-beans.xsd\t\thttp://www.springframework.org/schema/context\t\t   http://www.springframework.org/schema/context/spring-context.xsd");
			for (Iterator<Element> beans = iBeans.elementIterator(); beans
					.hasNext();) {
				Element bean = beans.next();
				Element oBean = oBeans.addElement("bean")
						.addAttribute("class", bean.attribute(0).getText())
						.addAttribute("id", bean.attribute(1).getText());
				if (bean.attribute(0).getText().contains("SuiteParameters")) {
					Element configurationParameters = oBean
							.addElement("property")
							.addAttribute("name", "ConfigurationParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.ConfigurationParameters");

					Element OamParameters = oBean
							.addElement("property")
							.addAttribute("name", "OamParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.OamParameters");

					for (Iterator<Element> properties = bean.elementIterator(); properties
							.hasNext();) {
						Element property = properties.next();
						String propertyName = property.attribute(0).getText();
						String value = property.attribute(1).getText();
						if (propertyName.contains("SuiteLevel")) {
							if (propertyName.contains("moAction")) {
								propertyName = "moActionsSuiteLevel";
							}
							configurationParameters
									.addElement("property")
									.addAttribute(
											"name",
											propertyName
													.substring(0, (propertyName
															.length() - 10)))
									.addAttribute("value", value.trim());
						} else if (propertyName.contains("oam")
								|| propertyName.contains("Oam")) {
							OamParameters.addElement("property")
									.addAttribute("name", propertyName)
									.addAttribute("value", value.trim());

						} else {
							oBean.addElement("property")
									.addAttribute("name", propertyName)
									.addAttribute("value", value.trim());
						}
					}
				} else {
					Map<String, Element> interfaceMap = new HashMap<>();
					Element trafficParameters = oBean
							.addElement("property")
							.addAttribute("name", "TrafficParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.TrafficParameters");
					interfaceMap.put("TrafficParameters", trafficParameters);

					Element aeroflexTrafficParameters = trafficParameters
							.addElement("property")
							.addAttribute("name", "AeroflexTrafficParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.AeroflexTrafficParameters");
					interfaceMap.put("AeroflexTrafficParameters",
							aeroflexTrafficParameters);

					Element lteSimTrafficParameters = trafficParameters
							.addElement("property")
							.addAttribute("name", "LteSimTrafficParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.LteSimTrafficParameters");
					interfaceMap.put("LteSimTrafficParameters",
							lteSimTrafficParameters);

					Element verdictParameters = oBean
							.addElement("property")
							.addAttribute("name", "VerdictParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.VerdictParameters");
					interfaceMap.put("VerdictParameters", verdictParameters);

					Element lteSimVerdictParameters = verdictParameters
							.addElement("property")
							.addAttribute("name", "LteSimVerdictParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.LteSimVerdictParameters");
					interfaceMap.put("LteSimVerdictParameters",
							lteSimVerdictParameters);

					Element configurationParameters = oBean
							.addElement("property")
							.addAttribute("name", "ConfigurationParameters")
							.addElement("bean")
							.addAttribute("class",
									"com.ericsson.msran.msr.interfaces.ConfigurationParameters");
					interfaceMap.put("ConfigurationParameters",
							configurationParameters);

					for (Iterator<Element> properties = bean.elementIterator(); properties
							.hasNext();) {
						Element property = properties.next();
						String propertyName = property.attribute(0).getText();
						if (propertyName.equals("moAction")) {
							propertyName = "moActions";
						}
						if (propertyName.equals("additionalSCList")) {
							propertyName = "additionalSystemConstants";
						}
						if (propertyName.equals("oamTestType")) {
							oBeans.elements()
									.get(0)
									.elements()
									.get(1)
									.elements()
									.get(0)
									.addElement("property")
									.addAttribute("name", propertyName)
									.addAttribute(
											"value",
											property.attribute(1).getText()
													.trim());
						}
						String key = getClassName(propertyName);
						if (!key.equals("skip")) {
							if (key.equals("TestParameters")) {
								oBean.addElement("property")
										.addAttribute("name", propertyName)
										.addAttribute(
												"value",
												property.attribute(1).getText()
														.trim());
							}
							for (Map.Entry<String, Element> entry : interfaceMap
									.entrySet()) {
								if (key.equals(entry.getKey())) {
									entry.getValue()
											.addElement("property")
											.addAttribute("name", propertyName)
											.addAttribute(
													"value",
													property.attribute(1)
															.getText().trim());
									break;
								}
							}
						}
					}
				}
			}

			FileWriter fileWriter = new FileWriter(outputDir.getPath()
					.concat("/").concat(path.getFileName().toString()));
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(fileWriter, format);
			writer.write(output);
			writer.close();
			System.out.println("==========" + path.getFileName().toString()
					+ ": done!===========");
			System.out.println("");
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getClassName(String input) {
		String tep[] = { "WithRecovery", "SampleCpuLoadAndIntensityScriptFile",
				"RunCpmBenchmarkTest", "SampleCpuLoadAndIntensity",
				"CreateGraphs", "TraceErrorCounter", "IterationOverride",
				"DumpDcgmLogs", "RunPgetCheck" };
		String trp[] = { "TestDuration", "WaitForNewRop", "IsPmCounterTest",
				"IsHandOverCase" };
		String ltp[] = { "TrafficScenario", "RecoveryStartInterval",
				"UeSetuprate", "NumBearerPerUe", "IpexLogging",
				"IpgwtgMaxReceiveWindow", "DistUeAcrossAreas", "CoLocatedIpex",
				"IsSimulatedCore", "DebugInfo", "IpexMtu",
				"EnodebCellNameToLtesimCellNameMap", "LtesimLoggingLevel",
				"StoreLteEventL3PerformanceStatistics" };
		String atp[] = { "TrafficScenarioString", "EnbProfileString",
				"DiversifEyeTestGroup", "ServiceDelayTime", "ServiceDuration",
				"CellRange", "RefSignalPower", "GetuestatusUes",
				"UeRrcRelease", "NumOfTM500Units", "AeroflexSetCaGroup",
				"AeroflexMtsCommands", "RunViaTma",
				"AeroflexCoreDumpMainFileName", "PowerCycleAfHL",
				"EnableAeroflexDebugging", "LoadSystemConfigurationId" };
		String vp[] = { "PmListAdditionTdd", "PmListAdditionFdd",
				"PmListCommon", "PmListAddition", "PmListCaCommon",
				"PmListCaAddition", "ValidateNrSePerTti",
				"ValidateNrSePerTtiPerCell", "GetStatMPloadVerifyFlag",
				"LowestAllowedTrafficIntensity", "UlMultiUserMimoValidate",
				"MinUlMultiUserPairsInCell", "CollectCceWaste",
				"CollectCfiMode", "LowestAllowedHandoverIntensity",
				"LowestAllowedRrcConnectionSuccessRatio",
				"LowestAllowedHandOverSuccessRatio", "MinAndMaxPaging",
				"TotalPdcpTputVerdict", "CellDlTputMax", "CellUlTputMax",
				"UeDlTputAvg", "UeUlTputAvg", "RrcConnSetupSuccRate",
				"InitialErabSetupSuccRate", "AddedERabEstabSuccRate",
				"ERabRetainability", "CellTransmissionModeVerdict",
				"CellRankVerdict", "VerdictConfigurationId",
				"ValidateDl256Qam", "ValidateUl64Qam", "ValidateUlCa",
				"ValidateLoadBasedTatAdjustment", "ValidateNrSePerTtiPmx",
				"CheckFairness", "CellsToBeChecked", "UeThresholdPct",
				"BearerThresholdPct", "GetstatVerifyFlag",
				"MaxRrcReleaseAllowed", "SrLatencyDistrCheck",
				"ValidateVarSrCqiPeriodicity", "IgnoreFirstRop",
				"CaTputValidate", "DlScaledTargetThroughput",
				"UlScaledTargetThroughput", "ValidateCatM" };
		String lvp[] = { "ValidateDLthroughput", "ValidateULthroughput",
				"PingRecoveryValue", "FtpRecoveryValue",
				"ValidateMaxMiSignaling", "PagingPerSecond", "ExpThroughput" };
		String cp[] = { "Tags", "MoActions", "AdditionalSystemConstants",
				"TInactivityTimer", "CaConfigSpec", "CaCellEarfcn",
				"CarrierAggregationParameters", "CellBandwidths",
				"AutoSetCellRelations", "ENBTracesCmds",
				"TraceOutputMechanism", "CreateScannerLocalFlag",
				"NoOfTxAntennas", "NoOfRxAntennas", "TransmissionMode",
				"SubframeAssignment", "SpecialSubframePattern", "IsCrossDuCa",
				"EranConfig", "RestoreCv", "ExpectedMaxNoOfUsersInCell",
				"TraceMeasurement" };

		Map<String, List<String>> mapping = new HashMap<>();
		mapping.put("TestParameters", Arrays.asList(tep));
		mapping.put("TrafficParameters", Arrays.asList(trp));
		mapping.put("AeroflexTrafficParameters", Arrays.asList(atp));
		mapping.put("LteSimTrafficParameters", Arrays.asList(ltp));
		mapping.put("VerdictParameters", Arrays.asList(vp));
		mapping.put("LteSimVerdictParameters", Arrays.asList(lvp));
		mapping.put("ConfigurationParameters", Arrays.asList(cp));
		
		for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
			for (String parameter : entry.getValue()) {
				if (parameter.equalsIgnoreCase(input)) {
					System.out.println(input + " -> " + entry.getKey());
					return entry.getKey();
				}
			}
		}
		if (!input.equalsIgnoreCase("oamTestType") && !input.equalsIgnoreCase("numBearer") && !input.equalsIgnoreCase("phyConfigUlInterference")) {
			set.add(input);
		}
		return "skip";

	}

	public static void format(String targetDir) {
		String s = "find ".concat(targetDir)
				.concat(" -type f -exec sed -i -r ");
		List<String> replaceCmds = new ArrayList<>();
		replaceCmds.add((s.concat("'s/~~         /~~\\n         /g' {} \\;")));
		replaceCmds.add((s.concat("'s/,         /,\\n         /g' {} \\;")));
		replaceCmds.add((s.concat("'s/;         /;\\n         /g' {} \\;")));
		replaceCmds
				.add((s.concat("'/^.*<property name=\"OamParameters\">/{:a;N;/^.*<\\/property>/!ba};/OamParameters\"\\/>/d' {} \\;")));
		replaceCmds
				.add((s.concat("'/^.*<property name=\"ConfigurationParameters\">/{:a;N;/^.*<\\/property>/!ba};/ConfigurationParameters\"\\/>/d' {} \\;")));
		replaceCmds
				.add((s.concat("'/^.*<property name=\"LteSimTrafficParameters\">/{:a;N;/^.*<\\/property>/!ba};/LteSimTrafficParameters\"\\/>/d' {} \\;")));
		replaceCmds
				.add((s.concat("'/^.*<property name=\"AeroflexTrafficParameters\">/{:a;N;/^.*<\\/property>/!ba};/AeroflexTrafficParameters\"\\/>/d' {} \\;")));
		replaceCmds
				.add((s.concat("'/^.*<property name=\"LteSimVerdictParameters\">/{:a;N;/^.*<\\/property>/!ba};/LteSimVerdictParameters\"\\/>/d' {} \\;")));
		replaceCmds.add("perl -p -i -e \"s/\\r/&#xD;/g\" ".concat(targetDir)
				.concat("/*"));

		String[] cmd = { "/bin/bash", "-c", "" };
		String[] check = { "/bin/bash", "-c", "echo $0" };
		try {
			Process checkShell = Runtime.getRuntime().exec(check);
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					checkShell.getInputStream()));
			String output = bf.readLine();
			if (output.equals("/bin/tcsh")) {
				System.out
						.println("Couldn't format the bean files, please change to bash shell and try again! (you're currently using tcsh)");
			} else if (output.equals("/bin/bash")) {
				for (String replaceCmd : replaceCmds) {
					cmd[2] = replaceCmd;
					Process p = Runtime.getRuntime().exec(cmd);
					p.waitFor();
					p.destroy();
				}
				System.out
				.println("Successfully formated all the bean files!");
			} else {
				System.out
				.println("Couldn't format the bean files, please change to bash shell and try again!");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}