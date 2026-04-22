package com.hulkhiretech.payments.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class User {

    @NotBlank(message = "END_USER_ID_BLANK")
    @Size(max = 64, message = "END_USER_ID_TOO_LONG")
    private String endUserID;

    @NotBlank(message = "FIRSTNAME_BLANK")
    @Size(max = 50, message = "FIRSTNAME_TOO_LONG")
    private String firstname;

    @NotBlank(message = "LASTNAME_BLANK")
    @Size(max = 50, message = "LASTNAME_TOO_LONG")
    private String lastname;

    @NotBlank(message = "EMAIL_BLANK")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "MOBILE_PHONE_BLANK")
    @Pattern(
        regexp = "^\\+?[0-9]{8,15}$",
        message = "MOBILE_PHONE_INVALID"
    )
    private String mobilePhone;
}