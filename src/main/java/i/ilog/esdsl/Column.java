package i.ilog.esdsl;


public class Column {
	String name;
	String type;
	String alias;
	Function function;
	String sortType;
    String sourceType;
    
    public String getSourceType(){
        return this.sourceType;
    }

    public void setSourceType(String sourceType){
        this.sourceType = sourceType;
    }

	public String getName() {
		return (this.sourceType!=null?(this.sourceType+"."):"")+name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(type == null) type = "asc";
		type = type.toLowerCase();
		this.type = type;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
	public String getSortType() {
		return sortType;
	}
	public void setSortType(String sortType) {
		if(sortType == null) sortType = "asc";
		sortType = sortType.toLowerCase();
		this.sortType = sortType;
	}
	public String toString(){
		
		/*return "{name:"+this.name+",type:"+this.type+",alias:"+this.alias
				+",function:"+this.function
				+",sortType:"+this.sortType
				+"}";**/
		return (this.sourceType!=null?(this.sourceType+"."):"")+name;
	}
	
	public Column(){}
	
	public Column(String name, String type,String alias) {
		super();
		this.name = name;
		this.type = type;
		this.alias = alias;
	}
	public Function getFunction() {
		return function;
	}
    public boolean isFunction(){
        return function != null;
    }
	public void setFunction(Function function) {
		this.function = function;
	}
	
	public String toScript(){
		if(function != null){
			return function.toScript();
		}else{
			return "doc['"+(this.sourceType!=null?(this.sourceType+"."):"")+this.name+"'].value";
		}
	}
	
	
}
