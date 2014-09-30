package i.ilog.esdsl;

import net.sf.json.JSONObject;

public interface ElementInterface {
	public JSONObject toJson();

    public void prepareHandle();
}
