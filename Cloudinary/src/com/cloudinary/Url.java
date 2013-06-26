package com.cloudinary;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

import com.cloudinary.Cloudinary.Configuration;

import android.text.TextUtils;

public class Url {
	private final Configuration config;
	String type = "upload";
	String resourceType = "image";
	String format = null;
	String version = null;
	Transformation transformation = null;

	public Url(Cloudinary cloudinary) {
		this.config = new Configuration(cloudinary.config);
	}

	public Url type(String type) {
		this.type = type;
		return this;
	}

	public Url resourcType(String resourceType) {
		return resourceType(resourceType);
	}
	
	public Url resourceType(String resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public Url format(String format) {
		this.format = format;
		return this;
	}

	public Url cloudName(String cloudName) {
		this.config.cloudName = cloudName;
		return this;
	}

	public Url secureDistribution(String secureDistribution) {
		this.config.secureDistribution = secureDistribution;
		return this;
	}

	public Url cname(String cname) {
		this.config.cname = cname;
		return this;
	}

	public Url version(Object version) {
		this.version = Cloudinary.asString(version);
		return this;
	}

	public Url transformation(Transformation transformation) {
		this.transformation = transformation;
		return this;
	}

	public Url secure(boolean secure) {
		this.config.secure = secure;
		return this;
	}

	public Url privateCdn(boolean privateCdn) {
		this.config.privateCdn = privateCdn;
		return this;
	}

	public Url cdnSubdomain(boolean cdnSubdomain) {
		this.config.cdnSubdomain = cdnSubdomain;
		return this;
	}

	public Url shorten(boolean shorten) {
		this.config.shorten = shorten;
		return this;
	}

	public Transformation transformation() {
		if (this.transformation == null)
			this.transformation = new Transformation();
		return this.transformation;
	}

	public String generate(String source) {
		if (type.equals("fetch") && !TextUtils.isEmpty(format)) {
			transformation().fetchFormat(format);
			this.format = null;
		}
		String transformationStr = transformation().generate();
		if (TextUtils.isEmpty(this.config.cloudName)) {
			throw new IllegalArgumentException("Must supply cloud_name in tag or in configuration");
		}

		if (source == null)
			return null;
		String original_source = source;

		if (source.toLowerCase(Locale.US).matches("^https?:/.*")) {
			if ("upload".equals(type) || "asset".equals(type)) {
				return original_source;
			}
			source = SmartUrlEncoder.encode(source);
		} else if (format != null) {
			source = source + "." + format;
		}
		if (config.secure && TextUtils.isEmpty(config.secureDistribution)) {
			config.secureDistribution = Cloudinary.SHARED_CDN;
		}
		String prefix;
		if (config.secure) {
			prefix = "https://" + config.secureDistribution;
		} else {
			CRC32 crc32 = new CRC32();
			crc32.update(source.getBytes());
			String subdomain = config.cdnSubdomain ? "a" + ((crc32.getValue() % 5 + 5) % 5 + 1) + "." : "";
			String host = config.cname != null ? config.cname : (config.privateCdn ? config.cloudName + "-" : "") + "res.cloudinary.com";
			prefix = "http://" + subdomain + host;
		}
		if (!config.privateCdn || (config.secure && Cloudinary.AKAMAI_SHARED_CDN.equals(config.secureDistribution)))
			prefix = prefix + "/" + config.cloudName;

		if (config.shorten && resourceType.equals("image") && type.equals("upload")) {
			resourceType = "iu";
			type = "";
		}

		if (source.contains("/") && !source.matches("v[0-9]+.*") && !source.matches("https?:/.*") && TextUtils.isEmpty(version)) {
			version = "1";
		}

		if (version == null)
			version = "";
		else
			version = "v" + version;

		return TextUtils.join("/", new String[] { prefix, resourceType, type, transformationStr, version, source }).replaceAll(
				"([^:])\\/+", "$1/");
	}
	
	@SuppressWarnings("unchecked")
	public String imageTag(String source) {
		return imageTag(source, Cloudinary.emptyMap());
	}

	public String imageTag(String source, Map<String, String> attributes) {
		String url = generate(source);
		attributes = new TreeMap<String, String>(attributes); // Make sure they are ordered.
		if (transformation().getHtmlHeight() != null)
			attributes.put("height", transformation().getHtmlHeight());
		if (transformation().getHtmlWidth() != null)
			attributes.put("width", transformation().getHtmlWidth());
		StringBuilder builder = new StringBuilder();
		builder.append("<img src='").append(url).append("'");
		for (Map.Entry<String, String> attr : attributes.entrySet()) {
			builder.append(" ").append(attr.getKey()).append("='").append(attr.getValue()).append("'");
		}
		builder.append("/>");
		return builder.toString();
	}
}
