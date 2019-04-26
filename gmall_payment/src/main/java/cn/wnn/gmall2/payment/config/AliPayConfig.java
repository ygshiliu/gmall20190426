package cn.wnn.gmall2.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by Administrator on 2018/6/12 0012.
 */
@Configuration
@PropertySource("classpath:alipay.properties")
public class AliPayConfig {
        @Value("${alipay_url}")
        private String alipay_url;
        @Value("${app_id}")
        private String app_id;
        @Value("${app_private_key}")
        private String app_private_key;

        public static String alipay_public_key;
        public static String return_payment_url;
        public static String notify_payment_url;
        public static String return_order_url;

        @Value("${alipay_public_key}")
        public void alipay_public_key(String alipay_public_key){
                AliPayConfig.alipay_public_key=alipay_public_key;
        }
        @Value("${return_payment_url}")
        public void  return_payment_url(String return_payment_url){
                AliPayConfig.return_payment_url=return_payment_url;
        }
        @Value("${notify_payment_url}")
        public void notify_payment_url(String notify_payment_url){
                AliPayConfig.notify_payment_url=notify_payment_url;
        }
        @Value("${return_order_url}")
        public void  return_order_url(String return_order_url){
                AliPayConfig.return_order_url=return_order_url;
        }

        public final static String format="json";
        public final static String charset="utf-8";
        public final static String sign_type="RSA2";

        @Bean
        public AlipayClient getAliPay(){
            AlipayClient alipayClient = new DefaultAlipayClient(alipay_url,app_id,app_private_key,format,charset,alipay_public_key,sign_type);
            return alipayClient;

        }



}
