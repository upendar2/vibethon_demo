package com.emailsender;
import java.util.Random;

public class GenOTP
{
    public static int generateOTP() 
    {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // generates number between 100000-999999
        return otp;
    }
}
