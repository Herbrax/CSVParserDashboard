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

    private static final String SEMICOLON = ":";
    private static final int RANDOM_COUNT = 10;
    private static final Random RANDOM = new Random();
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
        int totalCount = 0;
        Map<String, Integer> questionTypeCount = new HashMap<>();
        Map<String, Set<String>> workersPerQuestionType = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Question>>> questionTypeEntry : questionMap.entrySet()) {
            String questionType = questionTypeEntry.getKey();
            Map<String, List<Question>> workerMap = questionTypeEntry.getValue();

            questionTypeCount.put(questionType, 0);
            workersPerQuestionType.put(questionType, new HashSet<>());

            for (Map.Entry<String, List<Question>> workerEntry : workerMap.entrySet()) {
                String email = workerEntry.getKey();
                List<Question> questions = workerEntry.getValue();
                List<Question> selected = selectRandom(questions, RANDOM_COUNT);
                int count = selected.size();
                totalCount += count;

                questionTypeCount.put(questionType, questionTypeCount.get(questionType) + count);
                workersPerQuestionType.get(questionType).add(email);

                System.out.println("For worker email: " + email + ", question type: " + questionType + ", selected: " + count + " questions.");
                selectedQuestions.addAll(selected);
            }
        }

        System.out.println("Total selected questions: " + totalCount);

        for (String questionType : questionTypeCount.keySet()) {
            System.out.println("For question type: " + questionType + ", total questions: " + questionTypeCount.get(questionType) +
                    ", total workers: " + workersPerQuestionType.get(questionType).size());
        }

        return selectedQuestions;
    }



    private static List<Question> selectRandom(List<Question> list, int count) {
        List<Question> randomQuestions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if(list.size() > 0){
                randomQuestions.add(list.remove(RANDOM.nextInt(list.size())));
            }
        }
        return randomQuestions;
    }

    private static void writeCSV(List<Question> selectedQuestions, String filename) throws IOException {
        List<Question> sortedQuestions = selectedQuestions.stream()
            .sorted(Comparator.comparing(Question::getQuestionType).thenComparing(Question::getWorkerEmail))
            .collect(Collectors.toList());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("sep=;\n");
            writer.write("Question type;Worker email;QuestionID;Original QuestionID\n");
            for (Question question : sortedQuestions) {
                writer.write(question.toCSVString() + "\n");
            }
        }
    }
}
