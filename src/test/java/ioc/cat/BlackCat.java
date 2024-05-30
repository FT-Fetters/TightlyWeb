package ioc.cat;

import com.heybcat.tightlyweb.ioc.annotation.Cat;

@Cat
public class BlackCat {

    public BlackCat()
    {
        System.out.println("black cat init");
    }

    public String eat()
    {
        System.out.println("black cat eat");
        return "black cat eat";
    }



}
