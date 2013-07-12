package com.thedeanda.podcastpull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
	public static void main(String[] args) {
		// create the command line parser
		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();
		options.addOption("d", "dir", true,
				"target base directory, defaults to './data'");
		options.addOption("p", "podcast", true,
				"source url for podcast (required)");
		options.addOption("h", "help", false, "print this message");

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// validate that block-size has been set
			if (line.hasOption("help") || !line.hasOption("podcast")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("podcastpull", options);
				return;
			}

			String url = line.getOptionValue("podcast");
			String dir = line.getOptionValue("dir", "./data");

			PodcastPull pull = new PodcastPull(url);
			pull.setBasePath(dir);
			pull.run();

		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}

	}
}
