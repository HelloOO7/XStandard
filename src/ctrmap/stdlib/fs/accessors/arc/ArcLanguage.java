package ctrmap.stdlib.fs.accessors.arc;

public enum ArcLanguage {
	NULL("default"),
	JPN("Japanese"),
	ENG("English"),
	FRA("French"),
	ITA("Italian"),
	GER("German"),
	UNK_6("Unknown"),
	SPA("Spanish"),
	KOR("Korean");
	//Gen 6 didn't have Chinese language support yet, so the unknown language code remains a mystery

	private String friendlyName;

	private ArcLanguage(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Override
	public String toString() {
		return friendlyName;
	}

	public static ArcLanguage getLangForFriendlyName(String friendlyName) {
		for (ArcLanguage al : values()) {
			if (al.friendlyName.equals(friendlyName)) {
				return al;
			}
		}
		return ArcLanguage.NULL;
	}
}
