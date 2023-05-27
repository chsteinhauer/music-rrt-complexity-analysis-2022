import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Main {
    static final String dirPath = ""; // path to midi files
    static String outPath = ""; // path to output directory
    static final File dirFile = new File(dirPath);
    public static void main(String[] args) {
        try {
            outPath = outPath + "\\" + args[1];
            
            //Utils.MIDIToOPNDs(dirFile);
            //encode(args);
            //Utils.printToCSVFile(outPath);
            //AnalyseTECs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void AnalyseTECs() throws IOException {
        var files = Utils.getOutputFileList(outPath);
        var compressedSongs = files.stream().map(Main::compileData);

        compressedSongs.forEach(Main::analyseData);
        var datasets = compressedSongs.toList();
        Utils.printTECsToCSV(datasets, outPath);
    }

    private static void analyseData(Data data) {
        int vectorSum = 0, pointSum = 0, patternsSum = 0, size = 0;

        System.out.print("s" + data.code + " = [");
        data.tecs.sort(Comparator.comparing(tec -> -tec.patterns.size()));
        for (var tec : data.tecs) {
            pointSum += tec.points.size();
            vectorSum += tec.vectors.size();
            patternsSum += tec.patterns.size();

            var edges = tec.vectors.size() + 1;
            var nodes = tec.points.size();

            System.out.print(" " + edges + " " + nodes + "; ");
        }

        System.out.println("];");
//*/

    }

    private static Data compileData(File file) {
        try {
            return new Data(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void encode(String[] args) throws IOException, InterruptedException {
        var files = dirFile.listFiles();

        var parentDir = dirFile.getParent();

        for (var file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".opnd")) {
                String name = fileName.split("\\.")[0];
                var process = Runtime.getRuntime().exec(
                        " java -jar " + parentDir + "\\omnisia.jar -i " + dirPath + "\\" + fileName + " -o " + outPath + "\\" + name + " " + String.join(" ", args)
                );
            }
        }
    }
}