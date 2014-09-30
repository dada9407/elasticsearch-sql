package i.ilog.esdsl;

public class Term {
	String text="";
	public Term(String text){
		this.text = text;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public String toString(){
		return this.text;
	}
}
