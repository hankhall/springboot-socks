package com.hanka.entity;

import java.io.File;

public class Node {

	private String name;

	private String realName;

	private boolean folder;

	public Node(File file) {
		this.realName = file.getName();
		this.name = file.getName();
		this.folder = file.isDirectory();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}

}
