import com.chromamorph.notes.Notes;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Utils {
    static final String[] columns = new String[]{
            "numberOfNotes",
            "tatumsPerBar",
            "barOneStartsAt",
            "compressionRatio",
            "runningTime",
            "encodingLength",
            "encodingLengthWithoutResidualPointSet",
            "numberOfResidualPoints",
            "percentageOfResidualPoints",
            "compressionRatioWithoutResidualPointSet",
            "title",
            "numberOfTECs"
    };


    public static void printToCSVFile(String outPath) throws IOException {
        var out = new File(outPath);
        var files = Utils.getOutputFileList(outPath);

        var fileName = out.getName() + ".csv";
        Path outputFilePath = out.toPath().resolve(fileName);
        PrintWriter pr = new PrintWriter(outputFilePath.toFile());
        pr.println(String.join(", ", columns));

        for (var file : files) {
            List<String> row = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(file));

            String str;
            while ((str = br.readLine()) != null) {
                if (str.startsWith(columns[0]))
                    break;
            }

            for (var col : columns) {
                var value = str.split(" ")[1];
                row.add(value);
                str = br.readLine();
            }

            pr.println(String.join(", ", row));
            br.close();
        }

        pr.close();
    }


    public static void printTECsToCSV(List<Data> datasets, String outPath) throws IOException {
        var out = new File(outPath);

        var fileName = out.getName() + "Patterns.csv";
        Path outputFilePath = out.toPath().resolve(fileName);
        PrintWriter pr = new PrintWriter(outputFilePath.toFile());
        pr.println("Code, TECSize, PatternSize");

        for (var data : datasets) {
            if (data.tecs.size() > 25) {
                System.out.println(data.tecs.size());

                for (var tec : data.tecs) {
                    if (tec.points.size() < 2) continue;
                    pr.println(data.code + ", " + (tec.vectors.size() + 1) + ", " + tec.points.size());
                }
            }
        }

        pr.close();
    }

    public static List<File> getOutputFileList(String outPath) throws IOException {
        var dir = Paths.get(outPath);

        System.out.println(outPath);

        try (Stream<Path> filepath = Files.walk(dir))
        {
            return filepath.filter(Utils::isOutFile).map(Path::toFile).toList();
        }
    }

    private static boolean isOutFile(Path path) {
        var name = path.toFile().getName();
        return name.endsWith(".cos") || name.endsWith(".SIATECCompress") || name.endsWith(".RecurSIA") || name.endsWith(".SIATEC");
    }

    public static void MIDIToOPNDs(File dirFile) {
        var midis = dirFile.listFiles();

        for (var midi : midis) {
            if (midi.getName().endsWith(".mid") || midi.getName().endsWith(".midi")) {
                Utils.MIDIToOPND(new String[] {midi.getAbsolutePath()});
            }
        }
    }

    public static void MIDIToOPND(String[] args) {
        if (args.length == 0 || !args[0].toLowerCase().endsWith(".mid") && !args[0].toLowerCase().endsWith(".midi")) {
            System.out.println("You need to provide the path to a midi file as a command line argument.\nThe file must have the file extension .mid or .midi.");
        } else {
            try {
                Notes notes = Notes.fromMIDI(args[0], true, true);
                Path inputFilePath = Paths.get(args[0]);
                String inputFileName = inputFilePath.getFileName().toString();
                String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf(46)) + ".opnd";
                Path outputFilePath = inputFilePath.getParent().resolve(outputFileName);
                printToOPNDFile(notes, outputFilePath.toString());
            } catch (InvalidMidiDataException var6) {
                var6.printStackTrace();
            } catch (IOException var7) {
                var7.printStackTrace();
            }
        }
    }

    public static void printToOPNDFile(Notes notes, String opndFileName) {
        System.out.println("SIZE: " + notes.getNumberOfNotes());

        try {
            Path outputFilePath = Paths.get(opndFileName);
            outputFilePath.getParent().toFile().mkdirs();
            PrintWriter pr = new PrintWriter(outputFilePath.toFile());
            pr.println("");
            pr.print("(");
            for (var note : notes.getNotes()) {
                String pn = note.getPitchName();
                if (pn == null) {
                    pn = note.getComputedPitch().getPitchName();
                }
                var v = note.getChannel() + 1;

                pr.println("(" + note.getOnset() + " " + pn + " " + note.getDuration() + ")");
            }
            pr.print(")");
            pr.close();
        } catch (FileNotFoundException var8) {
            var8.printStackTrace();
        }

    }

    public static void awaitThenDestroy(Process process) {
        destroy(process,10);
    }

    private static void destroy(Process process, int count) {
        if (count == 0) {
            process.destroyForcibly();
        }

        CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS).execute(() -> {
            if(process.isAlive()) {
                destroy(process, count - 1);
            } else {
                process.destroy();
            }
        });
    }
}
