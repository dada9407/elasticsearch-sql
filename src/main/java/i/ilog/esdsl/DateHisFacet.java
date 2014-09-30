package i.ilog.esdsl;

import net.sf.json.JSONObject;

public class DateHisFacet implements ElementInterface,Facet{
	
	Column field;
	
	String interval;
	
	public DateHisFacet(){};
	
	
	
	public Column getField() {
		return field;
	}



	public void setField(Column field) {
		this.field = field;
	}



	public String getInterval() {
		return interval;
	}



	public void setInterval(String interval) {
		this.interval = interval;
	}



	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
			JSONObject facet = new JSONObject();
				JSONObject date_histogram = new JSONObject();
				date_histogram.put("field", field.getName());
				date_histogram.put("interval", interval);
			facet.put("date_histogram", date_histogram);
		json.put("facet", facet);
		return json;
	}

    @Override
    public void prepareHandle() {
    
    }

}
