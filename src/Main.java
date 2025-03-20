import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;

interface TextProcessor {
    public String process(String text);
}
class SimpleTextProcessor implements TextProcessor {
    public String process(String text) {
        return text;
    }
}
abstract class TextDecorator implements TextProcessor {
    protected TextProcessor wrapped;
    public TextDecorator(TextProcessor wrapped) {
        this.wrapped = wrapped;
    }
}
class UpperCaseDecorator extends TextDecorator {
    public UpperCaseDecorator(TextProcessor wrapped) {
        super(wrapped);
    }
    public String process(String text) {
        return wrapped.process(text).toUpperCase();
    }
}
class TrimDecorator extends TextDecorator {
    public TrimDecorator(TextProcessor wrapped) {
        super(wrapped);
    }
    public String process(String text) {
        return wrapped.process(text).trim();
    }
}
class ReplaceDecorator extends TextDecorator {
    public ReplaceDecorator(TextProcessor wrapped) {
        super(wrapped);
    }
    public String process(String text) {
        return wrapped.process(text).replaceAll(" ", "_");
    }
}
public class Main {
    public static void convertToUpperCase(String inputFile, String outputFile) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.toUpperCase());
                writer.newLine();
            }
            System.out.println("Файл успешно обработан и сохранен в " + outputFile);
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файла: " + e.getMessage());
        }
    }
    public static long readWithIO(String inputFile, String outputFile) {
        long startTime = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Файл успешно обработан и сохранен в " + outputFile);
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файла: " + e.getMessage());
        }
        return System.currentTimeMillis() - startTime;
    }
    private static long readWithNIO(String inputFile, String outputFile) {
        long startTime = System.currentTimeMillis();
        try (FileChannel inputChannel = FileChannel.open(Path.of(inputFile), StandardOpenOption.READ);
             FileChannel outputChannel = FileChannel.open(Path.of(outputFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            while (inputChannel.read(buffer) > 0) {
                buffer.flip();
                outputChannel.write(buffer);
                buffer.clear();
            }
            System.out.println("Файл успешно обработан и сохранен в " + outputFile);
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файла: " + e.getMessage());
        }
        return System.currentTimeMillis() - startTime;
    }
    public static void copyFile(String sourcePath, String destinationPath) {
        Path source = Path.of(sourcePath);
        Path destination = Path.of(destinationPath);
        if (!Files.exists(source)) {
            System.err.println("Ошибка: Исходный файл не найден");
            return;
        }
        try (FileChannel sourceChannel = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel destinationChannel = FileChannel.open(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
            System.out.println("Файл успешно скопирован");
        } catch (IOException e) {
            System.err.println("Ошибка при копировании файла: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        //1
        String inputFile = "input.txt";
        String outputFile = "output.txt";
        convertToUpperCase(inputFile, outputFile);
        //2
        TextProcessor processor = new ReplaceDecorator(new UpperCaseDecorator(new TrimDecorator(new SimpleTextProcessor() {})));
        String text = "Хаю-хай с вами Иван гай";
        System.out.println(processor.process(text));
        //3
        String inputFile2 = "input2.txt";
        String outputFile21 = "outputWithIO.txt";
        String outputFile22 = "outputWithNIO.txt";
        System.out.println("Время выполнения (IO): " + readWithIO(inputFile2, outputFile21));
        System.out.println("Время выполнения (NIO): " + readWithNIO(inputFile2, outputFile22));
        //4
        String sourceFile = "input3.txt";
        String destinationFile = "destination.txt";
        long startTime = System.nanoTime();
        copyFile(sourceFile, destinationFile);
        long endTime = System.nanoTime();
        System.out.println("Время выполнения: " + (endTime - startTime) / 1000000 + " мс");
    }
}