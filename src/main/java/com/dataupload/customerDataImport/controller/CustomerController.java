package com.dataupload.customerDataImport.controller;

import com.dataupload.customerDataImport.model.Customer;
import com.dataupload.customerDataImport.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @Value("${dataUrl}")
    private String dataUrl;



    @GetMapping("/dataImport")
    public String dataImport() {
        String stringline = "";
        List<Customer> customerList = new ArrayList<>();
        List<Customer> invalidcustomerList = new ArrayList<>();
        List uniqueCustomerList = new ArrayList<>();

        Date date=new Date();
        System.out.println(date + "Data process start");

        try {
            Resource resource = new ClassPathResource(dataUrl);
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            int i = 1;
            int sl = 1;
            StringBuffer validCustomerLine = new StringBuffer();
            StringBuffer csvString = new StringBuffer();
            StringBuffer invalidCustomerLine = new StringBuffer();

            while ((stringline = buffReader.readLine()) != null) {

                String[] customer = stringline.replace("/","").split(",");
                int customerSize = customer.length;

                String phoneNumber = customer[5];
                String email = customer[6];
                String uniqueKey = phoneNumber + "-" + email;
                String lastIndex = customerSize == 7 ? "" : customer[7];
                if (!uniqueCustomerList.contains(uniqueKey)) {
                    String phonePattern = "^(\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
                    Boolean phoneMatch = phoneNumber.matches(phonePattern);

                    String emailPattern = "^[A-Za-z0-9+_.]+@[a-z](.+)$";
                    Boolean emailMatch = email.matches(emailPattern);

                    if (phoneMatch == true && emailMatch == true) {
                        customerList.add(new Customer(customer[0], customer[1], customer[2], customer[3], customer[4], customer[5], customer[6], lastIndex));
                        validCustomerLine.append(stringline);
                        validCustomerLine.append('\n');

                        csvString.append(stringline);
                        csvString.append('\n');
                        if(i == 10000){
                            byte[] csvfile = csvString.toString().getBytes();
                            customerService.saveCSVData(csvfile,"/data/csvfile-"+sl+".txt");
                            csvString = new StringBuffer();
                            i=0;
                            sl++;
                        }
                        i++;

                    }
                    else {
                        invalidcustomerList.add(new Customer(customer[0], customer[1], customer[2], customer[3], customer[4], customer[5], customer[6], lastIndex));
                        invalidCustomerLine.append(stringline);
                        invalidCustomerLine.append('\n');
                    }
                    uniqueCustomerList.add(uniqueKey);
                }

            }
            buffReader.close();

            byte[] csvfile = csvString.toString().getBytes();
            customerService.saveCSVData(csvfile,"/data/csvfile-"+sl+".txt");
            customerService.dataImport(validCustomerLine,invalidCustomerLine,customerList,invalidcustomerList);

            System.out.println(new Date() + "Data process end");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Data has been imported";
    }

}
