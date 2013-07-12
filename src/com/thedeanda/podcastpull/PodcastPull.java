package com.thedeanda.podcastpull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class PodcastPull implements Runnable {
	private static final Logger log = Logger.getLogger(PodcastPull.class);
	private String url;
	private String basePath;
	private File targetDir;

	public PodcastPull(String url) {
		this.url = url;
		basePath = ".";
	}

	@Override
	public void run() {
		try {
			run_wrapped();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private void run_wrapped() throws Exception {
		URL feedUrl = new URL(url);

		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedUrl));

		log.info("Feed Title: " + feed.getTitle());
		this.targetDir = new File(basePath + File.separator + feed.getTitle());
		targetDir.mkdirs();

		// Get the entry items...
		for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
			log.debug("Title: " + entry.getTitle());
			log.debug("Unique Identifier: " + entry.getUri());
			log.debug("Updated Date: " + entry.getUpdatedDate());

			// Get the Links
			for (SyndLinkImpl link : (List<SyndLinkImpl>) entry.getLinks()) {
				log.debug("Link: " + link.getHref());
			}

			// Get the Contents
			for (SyndContentImpl content : (List<SyndContentImpl>) entry
					.getContents()) {
				log.debug("Content: " + content.getValue());
			}

			// Get the Categories
			for (SyndCategoryImpl category : (List<SyndCategoryImpl>) entry
					.getCategories()) {
				log.debug("Category: " + category.getName());
			}

			for (SyndEnclosure enclosure : (List<SyndEnclosure>) entry
					.getEnclosures()) {
				fetchEnclosure(enclosure);
			}
		}
	}

	private void fetchEnclosure(SyndEnclosure enclosure)
			throws MalformedURLException {
		URL fileUrl = new URL(enclosure.getUrl());

		String fileName = fileUrl.getFile();
		int index = fileName.lastIndexOf("/");
		if (index >= 0) {
			fileName = fileName.substring(index + 1);
		}
		log.debug("FileName: " + fileName);

		File targetFile = new File(targetDir, fileName);
		if (!targetFile.exists()) {
			log.debug("new file, download");
			FileOutputStream fos = null;
			InputStream is = null;
			try {
				fos = new FileOutputStream(targetFile);
				is = fileUrl.openStream();

				IOUtils.copy(is, fos);

			} catch (IOException e) {
				log.warn(e.getMessage(), e);

				if (fos != null) {
					try {
						fos.close();
						fos = null;
					} catch (IOException e1) {
						log.warn(e1.getMessage(), e1);
					}
				}

				if (is != null) {
					try {
						is.close();
					} catch (IOException e1) {
						log.warn(e1.getMessage(), e1);
					}
				}

				targetFile.delete();
			} finally {
				if (fos != null) {
					try {
						fos.flush();
						fos.close();
					} catch (IOException e) {
						log.warn(e.getMessage(), e);
					}
				}

				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						log.warn(e.getMessage(), e);
					}
				}
			}
		}

	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
