package conceptproofs;

import com.google.common.collect.Lists;
import config.WordsPreProcessor;
import hashing.HashingUtilities;
import kotlin.Pair;
import kotlin.jvm.functions.Function1;
import message.LocalBulletinBoard;
import message.MessageHub;
import model.Word;
import model.WordGenerator;
import model.WorkerFactory;
import model.worker.Worker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by stephen on 11/13/15.
 */
public class HashingPOC {
  private static HashingPOC ourInstance = new HashingPOC();

  public static HashingPOC getInstance() {
    return ourInstance;
  }

  private String dummyMessage = "that which with there have";

  private String hashAsString = null;

  Function1<String, Boolean> strSimpleEqualsHash = s -> {
    MessageDigest digest = HashingUtilities.INSTANCE.getPreppedMessageDigest(s);
    String localHashString = getStringBuilderFromMessageDigest(digest).toString();
    boolean equals = hashAsString.equals(localHashString);
    if (equals) {
      MessageHub.Companion.getFileBasedMessageHub().writeMessage("HashStampCollector", localHashString);
    }
    return equals;
  };

  private HashingPOC() {
    MessageDigest dumHasher = HashingUtilities.INSTANCE.getMessageDigest();
    byte[] dummyMessageBytes = dummyMessage.getBytes();
    dumHasher.update(dummyMessageBytes);

    StringBuilder builder =
        getStringBuilderFromMessageDigest(dumHasher);

    hashAsString = builder.toString();
  }

  // Because default parameters are hard
  public void run() {
    this.run(false);
  }

  public void run(boolean genFile) {

    System.out.println(String.format("Hashed to hex: %s", hashAsString));

    if (genFile) backUpToFile();

    Pair<List<Word>, List<Word>> filteredFoursAndFives =
        WordsPreProcessor.INSTANCE.getFilteredWordPartitionsBy(0, 20, s -> s.length() == 4);

    System.out.println(String.format("First = %s", filteredFoursAndFives.getFirst()));
    System.out.println(String.format("Second = %s", filteredFoursAndFives.getSecond()));

    List<Worker> workers = WorkerFactory.INSTANCE.findWorkers("thread");

    System.out.println("\n---------------------------------\n");
    workers.forEach(System.out::println);
    System.out.println("\n---------------------------------\n");

    Pair<List<String>, List<String>> foursAndFives =
        wordsToStringsListsPair(filteredFoursAndFives); // and repeat

    List<List<String>> partitionsOfFour =
        Lists.partition(foursAndFives.getFirst(), foursAndFives.getFirst().size() / workers.size() + 1);

    // SCRATCH: the previous permutations are being ignored
    WordGenerator.INSTANCE.seedGenerator(foursAndFives.getFirst());

    dispatchWorkers(strSimpleEqualsHash, workers, foursAndFives, partitionsOfFour);

    awaitCompletion();

  }

  @NotNull
  private Pair<List<String>, List<String>> wordsToStringsListsPair(Pair<List<Word>, List<Word>> filteredFoursAndFives) {
    return new Pair<>(filteredFoursAndFives.getFirst().stream() // stream the words
        .map(Word::component2) // into Strings
        .collect(Collectors.toList()), // get that list
        filteredFoursAndFives.getSecond().stream().map(Word::component2) // rinse
            .collect(Collectors.toList()));
  }

  private void dispatchWorkers(Function1<String, Boolean> hashPredicate,
                               List<Worker> workers,
                               Pair<List<String>, List<String>> foursAndFives,
                               List<List<String>> partitionsOfFour) {
    for (int i = 0, partitionsSize = partitionsOfFour.size(); i < partitionsSize; i++) {
      List<? extends String> partition = partitionsOfFour.get(i);
      workers.get(i).receiveBatch(
          hashPredicate, // predicate
          i, // particular partition
          foursAndFives.getSecond()); // all the five-lengths that one can handle
    }
  }

  private void awaitCompletion() {
    try {
      System.out.println("Main going to sleep until the job is done");
      Thread.sleep(Long.MAX_VALUE);
    } catch (InterruptedException e) {
      System.out.println("Main thread was interrupted. Time to check mail");

      MessageHub hub = MessageHub.Companion.getLocalMessageHub();
      if (hub instanceof LocalBulletinBoard) {
        System.err.println(String.format("The message is: %s", ( (LocalBulletinBoard) hub ).getNextMessage()));
      }
    }
  }

  @NotNull
  public static StringBuilder getStringBuilderFromMessageDigest(MessageDigest someHashGuy) {
    StringBuilder builder = new StringBuilder();
    for (byte _byte : someHashGuy.digest()) {
      builder.append(Integer.toString(( _byte & 0xFF ) + 0x100, 16).substring(1));
    }
    return builder;
  }

  /**
   * Writes the SHA-256, hex-string of "that which with there have" to file.
   * These five words are some of the most popular four and five letter words
   * (which is the problem domain)
   */
  private void backUpToFile() {
    try {
      Path path = Paths.get("src/config/dummy-SHA256.txt");
      if (!Files.exists(path)) {
        System.out.println("Made the file");
        Files.createFile(path);
      }
      Files.write(path, new ArrayList<CharSequence>() {
        {
          this.add(hashAsString);
        }
      });
      System.out.println("Wrote the file");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
