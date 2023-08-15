package Main;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CSVParser {
	// Live 
    private static final String SEMICOLON = ":";
    private static final Random RANDOM = new Random();
    private static final int TOTAL_SAMPLES_FOR_WEEK = 350;  

    public static void main(String[] args) throws IOException {
        String csvFile = "/Users/herbrax/Documents/GitHub/UttTest/input.csv";
        List<Question> questions = parseCSV(csvFile);
        Map<String, Map<String, List<Question>>> questionMap = buildQuestionMap(questions);
        List<Question> selectedQuestions = selectRandomQuestions(questionMap);
        writeCSV(selectedQuestions, csvFile.replace(".csv", "_output.csv"));
    }

    private static List<Question> parseCSV(String filename) {
        List<Question> questions = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(filename));
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {
            String[] lineInArray;
            while ((lineInArray = csvReader.readNext()) != null) {
                String questionType = lineInArray[0].trim();
                String workerEmail = lineInArray[1].trim();
                String questionId = lineInArray[3].trim();
                String originalQuestionId = lineInArray[5].trim();
                questions.add(new Question(questionType, workerEmail, questionId, originalQuestionId));
            }
        } catch (IOException | CsvValidationException e) {
            System.err.println("Error while parsing CSV file: " + e.getMessage());
        }
        return questions;
    }

    private static Map<String, Map<String, List<Question>>> buildQuestionMap(List<Question> questions) {
        return questions.stream().collect(
                Collectors.groupingBy(Question::getQuestionType,
                        Collectors.groupingBy(Question::getWorkerEmail)));
    }


    private static List<Question> selectRandomQuestions(Map<String, Map<String, List<Question>>> questionMap) {
        List<Question> selectedQuestions = new ArrayList<>();
        Map<String, Integer> questionTypeSampleCount = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Question>>> questionTypeEntry : questionMap.entrySet()) {
            String questionType = questionTypeEntry.getKey();
            Map<String, List<Question>> workerMap = questionTypeEntry.getValue();
            
            int totalSamplesAvailable = workerMap.values().stream().mapToInt(List::size).sum();
            int totalSelectedForThisType = 0;

            if(totalSamplesAvailable <= TOTAL_SAMPLES_FOR_WEEK) {
                for (List<Question> questions : workerMap.values()) {
                    selectedQuestions.addAll(questions);
                    totalSelectedForThisType += questions.size();
                }
                System.err.println("Warning: Total samples for question type " + questionType + " is less than TOTAL_SAMPLES_FOR_WEEK. Total available: " + totalSamplesAvailable);
            } else {
                int totalWorkers = workerMap.keySet().size();
                int samplesPerWorker = TOTAL_SAMPLES_FOR_WEEK / totalWorkers;
                int samplesToTake = samplesPerWorker;

                for (List<Question> questions : workerMap.values()) {
                    List<Question> selected = selectRandom(questions, samplesToTake);
                    totalSelectedForThisType += selected.size();
                    selectedQuestions.addAll(selected);
                }
            }

            questionTypeSampleCount.put(questionType, totalSelectedForThisType);
        }

        for (Map.Entry<String, Integer> entry : questionTypeSampleCount.entrySet()) {
            System.out.println("Total samples for question type " + entry.getKey() + ": " + entry.getValue());
        }

        return selectedQuestions;
    }



    private static List<Question> selectRandom(List<Question> list, int count) {
        List<Question> randomQuestions = new ArrayList<>();
        for (int i = 0; i < count && !list.isEmpty(); i++) {
            randomQuestions.add(list.remove(RANDOM.nextInt(list.size())));
        }
        return randomQuestions;
    }

    private static void writeCSV(List<Question> selectedQuestions, String filename) throws IOException {
        List<Question> sortedQuestions = selectedQuestions.stream()
            .sorted(Comparator.comparing(Question::getQuestionType).thenComparing(Question::getWorkerEmail))
            .collect(Collectors.toList());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("sep=;\n");
            writer.write("Question Type RV;Question Type TR; ;Reviewer email; ;Transcriber QuestionID;Link; ;Review QuestionID;Link\n");

            for (Question question : sortedQuestions) {
                writer.write(formatRow(question) + "\n");
            }
        }
    }

    private static String formatRow(Question question) {
        String columnA = question.getQuestionType();
        String columnB = question.getQuestionType() + "_transcription_review";
        String columnC = "";
        String columnD = question.getWorkerEmail();
        String columnE = "";
        String columnF = question.getOriginalQuestionId();
        String columnG = "https://crowdcompute-internal.corp.google.com/m/answers/" + columnF + "?poolid=default_worker_pool";
        String columnH = "";
        String columnI = question.getQuestionId();
        String columnJ = "https://crowdcompute-internal.corp.google.com/m/answers/" + columnI + "?poolid=default_worker_pool";

        return String.join(";", columnA, columnB, columnC, columnD, columnE, columnF, columnG, columnH, columnI, columnJ);
    }

}
