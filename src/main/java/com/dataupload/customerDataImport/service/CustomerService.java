package com.dataupload.customerDataImport.service;

import com.dataupload.customerDataImport.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${validCustomerData}")
    private String validCustomerData;

    @Value("${invalidCustomerData}")
    private String invalidCustomerData;

    public void saveCSVData(byte[] data, String filename ) throws IOException {
        Resource validCustomerResource = new ClassPathResource("/");
        File validCustomerFile = new File(validCustomerResource.getFile().getAbsolutePath() + "/../../src/main/resources" + filename);
        if (!validCustomerFile.exists()) {
            validCustomerFile.createNewFile();
        }
        FileOutputStream validCustomerOutSt = null;
        try {
            validCustomerOutSt = new FileOutputStream(validCustomerFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        validCustomerOutSt.write(data);
        validCustomerOutSt.close();
    }

    public void dataInsert(ArrayList <Customer> customerList,String sql)  {
        int batchSize = 1000;
        int numBatches = (int) Math.ceil((double) customerList.size() / batchSize);

        List<List<Customer>> batches = new ArrayList<>();
        for (int i = 0; i < numBatches; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, customerList.size());
            batches.add(customerList.subList(start, end));
        }


        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10000);
        taskExecutor.setMaxPoolSize(100000);
        taskExecutor.setQueueCapacity(10000);
        taskExecutor.initialize();

        for (List<Customer> batch : batches) {
            taskExecutor.execute(() -> {
                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Customer customer = batch.get(i);
                        ps.setString(1, customer.getFirstName());
                        ps.setString(2, customer.getLastName());
                        ps.setString(3, customer.getCity());
                        ps.setString(4, customer.getShortCode());
                        ps.setString(5, customer.getZipCode());
                        ps.setString(6, customer.getPhoneNumber());
                        ps.setString(7, customer.getEmail());
                        ps.setString(8, customer.getIp());
                    }

                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                });
            });
        }

        taskExecutor.shutdown();
    }
    public void dataImport(StringBuffer validCustomerLine, StringBuffer invalidCustomerLine, List<Customer> customerList, List<Customer> invalidcustomerList) throws IOException {
        //valid Data
        byte[] data = validCustomerLine.toString().getBytes();
        this.saveCSVData(data,validCustomerData);
        //Invalid Data
        byte[] invalidData = invalidCustomerLine.toString().getBytes();
        this.saveCSVData(invalidData,invalidCustomerData);
        System.out.println(new Date() + "Data write end");

        String sql = "INSERT INTO valid_customers (first_name, last_name, city,short_code, zip_code, phone_number,email,ip_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        this.dataInsert((ArrayList<Customer>) customerList,sql);
        sql = "INSERT INTO invalid_customers (first_name, last_name, city,short_code, zip_code, phone_number,email,ip_address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        this.dataInsert((ArrayList<Customer>) invalidcustomerList,sql);
    }
}
