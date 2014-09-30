package i.ilog.esdsl;

import java.util.List;

public class Function {
	String funName;
	List paramList;
	
	public String getFunName() {
		return funName;
	}
	public void setFunName(String funName) {
		this.funName = funName;
	}
	public List getParamList() {
		return paramList;
	}
	public void setParamList(List paramList) {
		this.paramList = paramList;
	}
	
	public Function() {
		super();
	}
	
	public String toString(){
		
		String pl = "[";
		boolean first = true;
		for(Object obj :paramList){
			pl += (first? "":",") + obj.toString();
			first = false;
		}
		pl += "]";
		return "{function Name:"+this.funName+",params:"+pl+"}";
	}
	
	public String toScript(){
		if(this.funName.equalsIgnoreCase("script")){
			return paramList.get(0).toString();
		}else{
			String paramstr="";
			boolean first = true;
			for(Object param : this.paramList){
				paramstr +=first?"":",";
				if(param instanceof Column){
					Column c = (Column)param;
					if(c.getFunction() != null){
						paramstr += c.getFunction().toScript();
					}else{
						paramstr += "doc['"+c.getName()+"'].value";
					}
				}else if(param instanceof String){
					paramstr += "'"+param+"'";
				}else if(param instanceof Integer){
					paramstr += ""+param;
				}
				first = false;
			}
			return this.funName+"("+paramstr+")";
		}
	}
	
}
