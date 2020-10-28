import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class LibsvmOperator {

	private static final String SPACE = " ";
	private static final String LIBSVM_TOOL_ROOT_PATH = "/Users/Leo/Knowledge/ML/HW/HW2/tool/libsvm-3.24/";
	private static final String LIBSVM_SCALE = "svm-scale";
	private static final String LIBSVM_TRAIN = "svm-train";
	private static final String LIBSVM_PREDICT = "svm-predict";
	private static final String INPUT_FILE_PATH = "/Users/Leo/Knowledge/ML/HW/HW2/src/Dataset/";
	private static final String OUTPUT_DATA_FILE_PATH = "/Users/Leo/Knowledge/ML/HW/HW2/output/data/";
	private static final String OUTPUT_MODEL_FILE_PATH = "/Users/Leo/Knowledge/ML/HW/HW2/output/model/";

	private static final Integer trainingDataNum = Integer.valueOf(3133);

	private String inputFileName = null;
	private String outputTrainingDataFileName = null;
	private String outputTestingDataFileName = null;
	private String rangeFileName = null;
	private String scaledOutputTrainingDataFileName = null;
	private String scaledOutputTestingDataFileName = null;
	private String modelFileName = null;

	/**
	 * Format dataset into libsvm format
	 */
	public void format() {

		String intputFileFullName = INPUT_FILE_PATH + this.inputFileName;
		this.outputTrainingDataFileName = "libsvm_training_" + this.inputFileName;
		String outputTrainingDataFileFullName = OUTPUT_DATA_FILE_PATH + this.outputTrainingDataFileName;
		this.outputTestingDataFileName = "libsvm_testing_" + this.inputFileName;
		String outputTestingDataFileFullName = OUTPUT_DATA_FILE_PATH + this.outputTestingDataFileName;

		System.out.println(String.format("inputFilePath=%s", intputFileFullName));
		System.out.println(String.format("outputTrainingDataFilePath=%s", outputTrainingDataFileFullName));
		System.out.println(String.format("outputTestingDataFilePath=%s", outputTestingDataFileFullName));

		File inputFile = new File(intputFileFullName);
		File outputTrainingFile = new File(outputTrainingDataFileFullName);
		File outputTestingFile = new File(outputTestingDataFileFullName);

		if (outputTrainingFile.exists()) {
			outputTrainingFile.delete();
		}
		try {
			outputTrainingFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Failed to create an output training file.");
			e.printStackTrace();
			return;
		}

		if (outputTestingFile.exists()) {
			outputTestingFile.delete();
		}
		try {
			outputTestingFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Failed to create an output testing file.");
			e.printStackTrace();
			return;
		}

		FileReader fr = null;
		BufferedReader br = null;
		FileWriter fw_training_data = null;
		BufferedWriter bw_training_data = null;
		FileWriter fw_testing_data = null;
		BufferedWriter bw_testing_data = null;
		Integer dataCounter = Integer.valueOf(0);
		BufferedWriter bw = null;

		try {

			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
			fw_training_data = new FileWriter(outputTrainingFile);
			bw_training_data = new BufferedWriter(fw_training_data);
			fw_testing_data = new FileWriter(outputTestingFile);
			bw_testing_data = new BufferedWriter(fw_testing_data);

			bw = bw_training_data;
			String str = null;
			while ((str = br.readLine()) != null) {

				// System.out.println("In:" + str);
				dataCounter++;
				String[] fields = str.split(",");

				StringBuilder sb = new StringBuilder();
				sb.append(fields[8]);
				sb.append(SPACE);
				for (int i = 0; i < fields.length - 1; i++) {

					sb.append(i + 1);
					sb.append(":");

					if (i == 0) {
						if ("M".equals(fields[i])) {
							sb.append(1);
						} else if ("F".equals(fields[i])) {
							sb.append(-1);
						} else if ("I".equals(fields[i])) {
							sb.append(0);
						}
					} else {
						sb.append(fields[i]);
					}

					if (i < fields.length - 2) {
						sb.append(SPACE);
					}

				}
				// System.out.println("Out:" + sb.toString());
				bw.write(sb.toString());
				bw.newLine();

				if (dataCounter.equals(trainingDataNum)) {

					System.out.println("Switch to generate testing data.");
					bw = bw_testing_data;

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (bw_training_data != null) {
				try {
					bw_training_data.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fw_training_data != null) {
				try {
					fw_training_data.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (bw_testing_data != null) {
				try {
					bw_testing_data.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (fw_testing_data != null) {
				try {
					fw_testing_data.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public void execCmd(String cmd) throws Exception {

		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);
		process.waitFor();
		System.out.println("scaleCmd exec result: " + process.exitValue());

		process.destroy();

	}

	public void scaleTrainingData() throws Exception {

		// ./svm-scale -l -1 -u 1 -s libsvm_scaled_abalone.range
		// ./data/libsvm_abalone_training.data > libsvm_scaled_abalone_training.data
		this.rangeFileName = this.inputFileName + ".range";
		this.scaledOutputTrainingDataFileName = "scaled_" + outputTrainingDataFileName;
		String scaleCmd = LIBSVM_TOOL_ROOT_PATH + LIBSVM_SCALE + " -l -1 -u 1 -s " + OUTPUT_DATA_FILE_PATH
				+ this.rangeFileName + " " + OUTPUT_DATA_FILE_PATH + outputTrainingDataFileName + " > "
				+ OUTPUT_DATA_FILE_PATH + scaledOutputTrainingDataFileName;
		System.out.println("scaleCmd=" + scaleCmd);
		this.execCmd(scaleCmd);

	}

	public void scaleTestingData() throws Exception {

		// ./svm-scale -r ./data/libsvm_scaled_abalone.range
		// ./data/libsvm_abalone_testing.data >
		// ./data/libsvm_scaled_abalone_testing.data
		this.scaledOutputTestingDataFileName = "scaled_" + this.outputTestingDataFileName;
		String scaleCmd = LIBSVM_TOOL_ROOT_PATH + LIBSVM_SCALE + " -r " + OUTPUT_DATA_FILE_PATH + this.rangeFileName
				+ " " + OUTPUT_DATA_FILE_PATH + outputTestingDataFileName + " > " + OUTPUT_DATA_FILE_PATH
				+ scaledOutputTestingDataFileName;
		System.out.println("scaleCmd=" + scaleCmd);
		this.execCmd(scaleCmd);

	}

	public void trainModel() throws Exception {

		// ./svm-train -s 4 ./data/libsvm_scaled_abalone_training.data
		// ./model/svm_abalone.model
		this.modelFileName = this.inputFileName + ".svm.model";
		String trainCmd = LIBSVM_TOOL_ROOT_PATH + LIBSVM_TRAIN + " -s 4 " + OUTPUT_DATA_FILE_PATH
				+ this.scaledOutputTrainingDataFileName + " " + OUTPUT_MODEL_FILE_PATH + modelFileName;
		System.out.println("trainCmd=" + trainCmd);
		this.execCmd(trainCmd);

	}

	public void validateModel() throws Exception {

		// ./svm-predict ./data/libsvm_scaled_abalone_testing.data
		// ./model/svm_abalone.model ./data/libsvm_scaled_abalone_testing.result
		String validateResultFileName = scaledOutputTestingDataFileName + ".result";
		String validateCmd = LIBSVM_TOOL_ROOT_PATH + LIBSVM_PREDICT + " " + OUTPUT_DATA_FILE_PATH
				+ this.scaledOutputTestingDataFileName + " " + OUTPUT_MODEL_FILE_PATH + modelFileName + " "
				+ OUTPUT_DATA_FILE_PATH + validateResultFileName;
		System.out.println("validateCmd=" + validateCmd);
		this.execCmd(validateCmd);

	}

	public static void main(String[] args) throws Exception {

		LibsvmOperator operator = new LibsvmOperator();
		if (args.length < 1) {
			System.out.println("Should have one input arguments.");
			return;
		}

		operator.inputFileName = args[0];

		// format input data into libsvm data format and split it into two files, the
		// training data and the testing data
		// Write the first 3133 records into the training data
		// Then, write the rest records, 1044, into the testing data
		operator.format();

		// Scale the training data into [-1, +1] and save the range scaling file
		operator.scaleTrainingData();

		// Use scaled training data to train the model
		operator.trainModel();

		// Use previous saved range scaling file to scale the testing data
		operator.scaleTestingData();
		
		// Finally, use the scaled testing data to test the model trained by the training data
		operator.validateModel();

	}

}
