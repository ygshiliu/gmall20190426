package cn.wnn.gmall2.webutil;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * Created by Administrator on 2018/5/15 0015.
 */
public class JwtUtil {

    public static String encode(String key, Map<String,Object> param, String salt){
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(param);

        String token = jwtBuilder.compact();
        return token;

    }


    public  static Map<String,Object>  decode(String token ,String key,String salt) throws JwtException{
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return  claims;
    }
}
