package com.sofia.uni.fmi.web.primefaces.mapper;

import java.util.HashMap;
import java.util.Map;

public class ImagesMapper {

	private Map<Image, String> images;

	public ImagesMapper() {
		this.images = new HashMap<>();
		this.images.put(Image.SUSE, Image.SUSE.getId());
		this.images.put(Image.WINDOWS, Image.WINDOWS.getId());
		this.images.put(Image.UBUNTU, Image.UBUNTU.getId());
	}

	public Map<Image, String> getImages() {
		return images;
	}

}
