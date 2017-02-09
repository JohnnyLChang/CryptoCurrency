/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testcalculate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class Logic{
    private Random random = new Random();
    
    //Ger random value from 0 to 365
    public Integer GetRandomBirthday(){
        int day = random.nextInt(365);
        return day;
    }
    
    //Initialize the Array for Birthday according to size
    public void InitBirthday(ArrayList<Integer> birthday, Integer size, Integer rounds){
        random.setSeed(rounds);
        for(int j=0;j<size;j++)
                birthday.add(GetRandomBirthday());
    }
    
    //Check if there is any duplicated birthday
    public boolean IsDulicateBirthday(ArrayList<Integer> birthday){
        Set<Integer> setBirthday = new HashSet<Integer>();
        for(Integer day : birthday){
            if(setBirthday.contains(day))
            {
                //find a duplicate birthday
                return true;
            }
            else
                setBirthday.add(day);
        }
        return false;
    }
    
    private double CalculateProb(Integer nCount, Integer size){
        double dout=(double)nCount*100/(double)size;   
        BigDecimal bd= new BigDecimal(dout);   
        bd = bd.setScale(3, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
    
    public double calculate(int size, int count) {
        int nDuplicate = 0;
        ArrayList<Integer> birthday = new ArrayList<>(size);
        for(int i=0;i<count;i++)
        {
            birthday.clear();
            InitBirthday(birthday, size, i);
            if(IsDulicateBirthday(birthday))
                nDuplicate++;
        }
        
        return CalculateProb(nDuplicate, count);
    }
}
/**
 *
 * @author river
 */
public class TestCalculate {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Logic mLogic = new Logic();
        Random g = new Random();
        System.out.println("5, 10000 = " + mLogic.calculate(5, 10000));
        for (int i = 0; i < 1; i++) {
            System.out.println("test:"+i);
            //double result = mLogic.calculate(g.nextInt(400), g.nextInt(100000));
            //System.out.println(result);
        }
        
        //System.out.println("20, 10000 = " + logic.calculate(20, 10000));
    }
    
}
