package eu.fbk.dkm.pikes.raid;

import com.google.common.collect.Sets;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.dkm.utils.eval.SetPrecisionRecall;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

/**
 * Created by alessio on 08/05/15.
 */

public class Evaluation {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Evaluation.class);

	public static void main(String[] args) {
		try {
			final CommandLine cmd = CommandLine
					.parser()
					.withName("yamcha-evaluator")
					.withHeader("Evaluate YAMCHA classification")
					.withOption("i", "input-file", "the test file annotated", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
//					.withOption("g", "gold", "gold column (starting from 0)", "NUM", CommandLine.Type.POSITIVE_INTEGER, true, false, true)
//					.withOption("t", "test", "test column (starting from 0)", "NUM", CommandLine.Type.POSITIVE_INTEGER, true, false, true)
					.withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

			File testFile = cmd.getOptionValue("i", File.class);
//			Integer goldCol = cmd.getOptionValue("g", Integer.class);
//			Integer testCol = cmd.getOptionValue("t", Integer.class);

			SetPrecisionRecall.Evaluator e = SetPrecisionRecall.evaluator();

			BufferedReader reader = new BufferedReader(new FileReader(testFile));

			String line;
			Set<Set<Integer>> goldSpans = Sets.newHashSet();
			Set<Set<Integer>> testSpans = Sets.newHashSet();
			Set<Integer> thisGoldSpan = Sets.newHashSet();
			Set<Integer> thisTestSpan = Sets.newHashSet();

//			Integer goldCol = -1;
//			Integer testCol = -1;

			int i = 0;
			while ((line = reader.readLine()) != null) {
				i++;

				LOGGER.debug("{} --- {} - {}", i,
						line.substring(0, Math.min(20, line.length())),
						line.substring(Math.max(0, line.length() - 10)));

				if (line.trim().length() == 0) {
					LOGGER.debug(goldSpans.toString());
					LOGGER.debug(testSpans.toString());
					e.add(goldSpans, testSpans);
					goldSpans = Sets.newHashSet();
					testSpans = Sets.newHashSet();
				}

				String[] parts = line.split("\\s");
				if (parts.length < 2) {
					continue;
				}

				int testCol = parts.length - 1;
				int goldCol = parts.length - 2;

//				if (parts.length > 0 && parts[0].trim().length() > 0 && goldCol.equals(-1) && testCol.equals(-1)) {
//					testCol = parts.length - 1;
//					goldCol = parts.length - 2;
//				}
//
//				if (parts.length <= Math.max(testCol, goldCol)) {
//					if (parts.length > 0 && parts[0].trim().length() > 0) {
//						LOGGER.warn("Column count problem in line {}", i);
//					}
//					continue;
//				}

				if (parts[goldCol].equals("O")) {
					if (thisGoldSpan.size() > 0) {
						goldSpans.add(thisGoldSpan);
					}
					thisGoldSpan = Sets.newHashSet();
				}
				else {
					thisGoldSpan.add(i);
				}
				if (parts[testCol].equals("O")) {
					if (thisTestSpan.size() > 0) {
						testSpans.add(thisTestSpan);
					}
					thisTestSpan = Sets.newHashSet();
				}
				else {
					thisTestSpan.add(i);
				}

//				System.out.println(parts[testCol]);
//				System.out.println(parts[goldCol]);
//				System.out.println();
			}
			LOGGER.debug(goldSpans.toString());
			e.add(goldSpans, testSpans);

			SetPrecisionRecall spr = e.getResult();
			System.out.println(spr);

			reader.close();
		} catch (final Throwable ex) {
			CommandLine.fail(ex);
		}

	}
}
