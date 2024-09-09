package com.bulmeong.basecamp.campingcar.dto; 

import java.util.Date;

import lombok.Data;

@Data
public class RentalReview {
    private int id;
    private int reservation_id;
    private int rate;
    private String content;
    private Date created_at;
    private String reply_content;
    private Date reply_date;

}
                          