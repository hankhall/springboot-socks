package com.hanka.web;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hanka.entity.Node;

@Controller
public class IndexController {
	private String themes = "TouchOptimizedGallery";

	@Value("${dir.foto}")
	private String fotoPath;
	@Value("${dir.cover}")
	private String coverPath;
	@Value("${dir.prv}")
	private String prvPath;

	@GetMapping("/")
	public String index(Model model) {
		return index(model, "", "");
	}

	@GetMapping("/{path}")
	public String index(Model model, @PathVariable("path") String path, String mc) {
		model.addAttribute("path", path);
		model.addAttribute("mc", mc);
		List<Node> list = new ArrayList<Node>();
		model.addAttribute("list", list);
		for (File file : getFoto(path).listFiles()) {
			Node node = new Node(file);
			list.add(node);

		}
		return themes + "/index";
	}

	@RequestMapping(value = { "/foto/{path}" }, method = { RequestMethod.GET }, params = { "mc" })
	public String makecover(@PathVariable String path, String mc) throws IOException {
		makeCover(mc, path, getCvr(mc));
		return "redirect:/" + URLEncoder.encode(mc, "UTF8");
	}

	@RequestMapping(value = { "/foto/{path}" }, method = { RequestMethod.GET }, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<FileSystemResource> foto(@PathVariable String path) {
		return getImg(getFoto(path));
	}

	@RequestMapping(value = { "/prv/{path}" }, method = { RequestMethod.GET }, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<FileSystemResource> previewImg(@PathVariable String path) throws IOException {
		return getImg(getPrv(path));
	}

	@RequestMapping(value = { "/cvr/{path}" }, method = { RequestMethod.GET }, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<FileSystemResource> previewCvr(@PathVariable String path) throws IOException {
		File file = getCvr(path);
		if (file.exists()) {
			return getImg(file);
		}
		makeCover(path, "", file);
		return getImg(file);
	}

	private File getCvr(String path) {
		String filePath = coverPath + '/' + path.replaceAll(":", "_");
		return new File(filePath);
	}

	private void makeCover(String path, String firstPath, File file) throws IOException {
		List<File> files = get4Files(path, firstPath);
		int width = 170;
		BufferedImage buffImg = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = buffImg.createGraphics();
		graphics.setColor(new Color(255, 233, 161));
		graphics.fillRect(0, 0, width, width);
		graphics.setColor(new Color(255, 255, 255));

		for (int i = files.size() - 1; i >= 0; i--) {
			graphics.fillRect(i * 15, i * 15, 125, 125);
			graphics.drawImage(getSubSquare(files.get(i), 119), i * 15 + 3, i * 15 + 3, null);

		}
		ImageIO.write(buffImg, "JPEG", file);
	}

	private Image getSubSquare(File file, int size) throws IOException {
		BufferedImage bi = ImageIO.read(file);
		int w = bi.getWidth();
		int h = bi.getHeight();
		int s = w > h ? h : w;
		return bi.getSubimage((w - s) / 2, (h - s) / 2, s, s).getScaledInstance(size, size, Image.SCALE_SMOOTH);

	}

	private List<File> get4Files(String path, String firstPath) throws IOException {
		List<File> fs = new ArrayList<File>();
		if (StringUtils.isNotEmpty(firstPath)) {
			File file = getFoto(firstPath);
			fs.add(file);
			firstPath = file.getCanonicalPath();
		}
		put4Files(fs, getFoto(path).listFiles(), firstPath);
		return fs;
	}

	private void put4Files(List<File> fs, File[] listFiles, String firstPath) throws IOException {
		List<File> dirs = new ArrayList<File>();
		for (File f : listFiles) {
			if (f.isFile()) {
				if (!f.getCanonicalPath().equals(firstPath)) {
					fs.add(f);
					if (fs.size() >= 4) {
						return;
					}
				}
			} else {
				dirs.add(f);
			}
		}
		for (File d : dirs) {
			put4Files(fs, d.listFiles(), firstPath);
			if (fs.size() >= 4) {
				return;
			}
		}
	}

	private File getFoto(String path) {
		return new File(fotoPath + path.replaceAll(":", "/"));
	}

	private File getPrv(String path) throws IOException {
		File file = new File(prvPath + path.replaceAll(":", "/"));
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			int width = 170;
			BufferedImage buffImg = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = buffImg.createGraphics();
			graphics.drawImage(getSubSquare(getFoto(path), width), 0, 0, null);
			ImageIO.write(buffImg, "JPEG", file);
		}
		return file;
	}

	private ResponseEntity<FileSystemResource> getImg(File file) {
		FileSystemResource body = new FileSystemResource(file);
		ResponseEntity<FileSystemResource> response = ResponseEntity.ok()
				.header("Content-Disposition", "inline; filename=" + file.getName())
				.cacheControl(CacheControl.maxAge(10, TimeUnit.DAYS)).body(body);
		return response;
	}

}
