package сom.integral.domain;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationReport {
    private String status;
    private List<String> remarks = new ArrayList<>();

    public void addRemark(String remark){
        remarks.add(remark);
    }

    public boolean isAccepted(){
        return remarks.isEmpty();
    }
}
