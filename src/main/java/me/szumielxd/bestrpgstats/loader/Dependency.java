package me.szumielxd.bestrpgstats.loader;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public enum Dependency {
	
	
	MARIADB(Repository.MAVEN_CENTRAL, "org.mariadb.jdbc", "mariadb-java-client", "2.7.4", "9ca3eac191f81a8c0062e35de6e35ed8"),
	HIKARICP(Repository.MAVEN_CENTRAL, "com,zaxxer", "HikariCP", "4.0.3", "e725642926105cd1bbf4ad7fdff5d5a9"),
	;
	
	
	@Getter private final String downloadUrl;
	@Getter private final Repository repository;
	@Getter private final String groupId;
	@Getter private final String artifactId;
	@Getter private final String version;
	@Getter private final String md5;
	
	
	private Dependency(Repository repo, String groupId, String artifactId, String version, String md5) {
		groupId = groupId.replace(',', '.');
		this.downloadUrl = repo.getUrl() + '/' + groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/' + artifactId + '-' + version + ".jar";
		this.repository = repo;
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.md5 = md5;
	}
	
	
	public String getFileName() {
		return this.artifactId + ".jar";
	}
	
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static enum Repository {
		
		
		MAVEN_CENTRAL("https://repo1.maven.org/maven2"),
		;
		
		
		@Getter private final String url;
		
	}
	

}
