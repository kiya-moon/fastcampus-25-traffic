package com.fastcampus.springbatch.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Customer {
    private String id;
    private String name;
    private String email;
    private LocalDateTime registeredDate;
}
