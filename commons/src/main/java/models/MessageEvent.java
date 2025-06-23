package models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class MessageEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String receiverName;

    private String mailID;

    private String subject;

    private String body;

    private String category;

    public MessageEvent(){}

    @Override
    public String toString() {
        return "MessageEvent{" +
                "receiverName='" + receiverName + '\'' +
                ", mailID='" + mailID + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
