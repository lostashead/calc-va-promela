package jacksongradle;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Component {
	public Component() {}
    @JsonProperty("state")
    private String state;//状態名

    @JsonProperty("afterconnector")
    private List afterconnector;//直後の状態名

    @JsonProperty("alternative")
    private List alternative;//alernativeの有無

    @JsonProperty("optional")
    private List optional;//optionalの有無

    @JsonProperty("probability")
    private List probability;//可変性部の確率（シミュレーション後）

    //セッターとゲッターに注意
    public String getState(){
        return state;
    }
    public void setState(String state){
        this.state = state;
    }
    public List getAfterConnector(){
        return afterconnector;
    }
    public void setAfterConnector(List afterconnector){
        this.afterconnector = afterconnector;
    }
    public List getAlternative(){
        return alternative;
    }
    public void setAlternative(List alternative){
        this.alternative = alternative;
    }
    public List getOptional(){
        return optional;
    }
    public void setOptional(List optional){
        this.optional = optional;
    }
    public List getProbability(){
        return probability;
    }
    public void getProbability(List probability){
        this.probability = probability;
    }
}
