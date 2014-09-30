package i.ilog.esdsl;

import java.util.List;

import net.sf.json.JSONObject;

public class OperExp extends BooleanExp {
	private Object l;
	private String oper;
	private Object r;
	
	public OperExp(Object l,String oper,Object r){
		this.l = l;
		this.oper = oper;
		this.r = r;
	}
	
	@Override
	public JSONObject toJson() {
		boolean lIsCol = l instanceof Column;
		boolean rIsCol = r instanceof Column;
		boolean lIsStr = l instanceof String;
		boolean lIsInt = l instanceof Integer;
		boolean rIsStr = l instanceof String;
		boolean rIsInt = l instanceof Integer;
		Column lCol = lIsCol ? (Column)l : null;
		Column rCol = rIsCol ? (Column)r : null;
		JSONObject json = new JSONObject();
		if(oper.equalsIgnoreCase("like")){
			JSONObject regexp = new JSONObject();
			regexp.put(lCol.getName(),r);
			json.put("regexp",regexp);
		}else if(oper.equalsIgnoreCase("in")){
			InExp in = new InExp();
			in.setColumn(lCol);
            if(r instanceof EsQueryBuilder){
                in.setBuilder((EsQueryBuilder)r);
            }else{
                in.setValueList((List)r);
            }
			//return in.toJson();
		}else if(oper.equalsIgnoreCase("not in")){
				NotInExp notIn = new NotInExp();
			notIn.setColumn(lCol);
            if(r instanceof EsQueryBuilder){
                notIn.setBuilder((EsQueryBuilder)r);
            }else{
                notIn.setValueList((List)r);
            }
			//return notIn.toJson();
		}else if(lIsCol && lCol.getFunction()!=null || rIsCol && rCol.getFunction()!=null){
			String op = (oper.equals("=") || oper.equals(":")) ? "==" : oper;
		    String exp= lIsCol? (lCol.getFunction()!=null?lCol.getFunction().toScript():"doc['"+lCol.getName()+"'].value") : (lIsStr?"'"+l+"'":""+l);
		    exp +=op;
		    exp += rIsCol? (rCol.getFunction()!=null?rCol.getFunction().toScript():"doc['"+rCol.getName()+"'].value") : (rIsStr?"'"+r+"'":""+r);
		    JSONObject script = new JSONObject();
		    script.put("script",exp);
		    json.put("script",script);
		}else if(lIsCol && rIsCol){
			if(oper.equals(":")){
				if(!rCol.getName().equals("*")){
					JSONObject term = new JSONObject();
				    term.put(((Column)l).getName(),rCol.getName());
				    //json.put("term",term);
				}
			}else{
				String op = (oper.equals("=") || oper.equals(":")) ? "==" : oper;
			    String exp="doc['"+lCol.getName()+"'].value"+ op +"doc['"+rCol.getName()+"'].value";
			    JSONObject script = new JSONObject();
			    script.put("script",exp);
			    json.put("script",script);
			}
			
		}else if((l instanceof String || l instanceof Integer) && (r instanceof Integer || r instanceof String)){
			String op = (oper.equals("=") || oper.equals(":")) ? "==" : oper;
			String exp="";
		    if(l instanceof String){
		    	exp="'"+l+"'"+op;
		    }else{
		    	exp=l+op;
		    }
		    
		    if(r instanceof String){
		    	exp += "'"+r+"'";
		    }else{
		    	exp += r;
		    }
		    JSONObject script = new JSONObject();
		    script.put("script",exp);
		    json.put("script",script);
		}else if(oper.equals(":")){
			JSONObject term = new JSONObject();
		    term.put(((Column)l).getName(),r);
		    //json.put("term",term);

		}else if(lIsCol){
			JSONObject rang = new JSONObject();
			JSONObject operMap = new JSONObject();
			if(oper.equals("=")){
				JSONObject term = new JSONObject();
				term.put(lCol.getName(),r);
			    //json.put("term",term);
			}else if(oper.equals(">")){
                if(r instanceof String || r instanceof Term){
                    operMap.put("gt",String.valueOf(r));
                }else{
                    operMap.put("gt",Integer.parseInt(String.valueOf(r)));
                }
			   	rang.put(lCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("<")){
                if(r instanceof String || r instanceof Term){
				    operMap.put("lt",String.valueOf(r));
                }else{
                    operMap.put("lt",Integer.parseInt(String.valueOf(r)));
                }
			   	rang.put(lCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals(">=")){
                if(r instanceof String || r instanceof Term){
                    operMap.put("gte",String.valueOf(r));
                }else{
                    operMap.put("gte",Integer.parseInt(String.valueOf(r)));
                }
			   	rang.put(lCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("<=")){
                if(r instanceof String || r instanceof Term){
                    operMap.put("lte",String.valueOf(r));
                }else{
                    operMap.put("lte",Integer.parseInt(String.valueOf(r)));
                }
			   	rang.put(lCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("!=") || oper.equals("<>")){
				JSONObject term = new JSONObject();
				JSONObject termWrap = new JSONObject();
				term.put(lCol.getName(),r);
				termWrap.put("term",term);
			    json.put("not",termWrap);
			}
		}else if(rIsCol){
			JSONObject rang = new JSONObject();
			JSONObject operMap = new JSONObject();
			if(oper.equals("=")){
				JSONObject term = new JSONObject();
				term.put(rCol.getName(),l);
			    //json.put("term",term);
			}else if(oper.equals(">")){
                if(l instanceof String || l instanceof Term){
                    operMap.put("gt",String.valueOf(l));
                }else{
                    operMap.put("gt",Integer.parseInt(String.valueOf(l)));
                }
			   	rang.put(rCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("<")){
                if(l instanceof String || l instanceof Term){
                    operMap.put("lt",String.valueOf(l));
                }else{
                    operMap.put("lt",Integer.parseInt(String.valueOf(l)));
                }
			   	rang.put(rCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals(">=")){
                if(l instanceof String || l instanceof Term){
                    operMap.put("gte",String.valueOf(l));
                }else{
                    operMap.put("gte",Integer.parseInt(String.valueOf(l)));
                }
			   	rang.put(rCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("<=")){
                if(l instanceof String || l instanceof Term){
                    operMap.put("lte",String.valueOf(l));
                }else{
                    operMap.put("lte",Integer.parseInt(String.valueOf(l)));
                }
			   	rang.put(rCol.getName(),operMap);
				json.put("range", rang);
			}else if(oper.equals("!=") || oper.equals("<>")){
				JSONObject term = new JSONObject();
				JSONObject termWrap = new JSONObject();
				term.put(rCol.getName(),l);
				termWrap.put("term",term);
			    json.put("not",termWrap);
			}
		}
		
		return json;
	}
	
	@Override
	public String toQueryString() {
		boolean lIsCol = l instanceof Column;
		boolean rIsCol = r instanceof Column;
		boolean lIsStr = l instanceof String;
		boolean lIsInt = l instanceof Integer;
		boolean rIsStr = l instanceof String;
		boolean rIsInt = l instanceof Integer;
		Column lCol = lIsCol ? (Column)l : null;
		Column rCol = rIsCol ? (Column)r : null;
		if(oper.equalsIgnoreCase("in")){
			InExp in = new InExp();
			in.setColumn(lCol);
            if(r instanceof EsQueryBuilder){
                in.setBuilder((EsQueryBuilder)r);
            }else{
                in.setValueList((List)r);
            }
			return in.toQueryString();
		}else if(oper.equalsIgnoreCase("not in")){
			NotInExp notIn = new NotInExp();
			notIn.setColumn(lCol);
            if(r instanceof EsQueryBuilder){
                notIn.setBuilder((EsQueryBuilder)r);
            }else{
                notIn.setValueList((List)r);
            }
			return notIn.toQueryString();
		}else if(oper.equals(":")){
			return lCol.getName()+":"+(r instanceof Term?"\""+r+"\"":r);
		}else if(lIsCol && !rIsCol){
			if(oper.equals(":") || oper.equals("=")){
				return lCol.getName()+":"+(r instanceof Term?"\""+r+"\"":r);
			}else if(oper.equals("!=") || oper.equals("<>")){
				return "NOT "+lCol.getName()+":"+(r instanceof Term?"\""+r+"\"":r);
			}
		}else if(rIsCol && !lIsCol){
			if(oper.equals(":") || oper.equals("=") || oper.equals("like")){
				return rCol.getName()+":"+(l instanceof Term?"\""+l+"\"":l);
			}else if(oper.equals("!=") || oper.equals("<>")){
				return "NOT "+rCol.getName()+":"+(l instanceof Term?"\""+l+"\"":l);
			}
		}else{
			return null;
		}
		return null;
	}
}
