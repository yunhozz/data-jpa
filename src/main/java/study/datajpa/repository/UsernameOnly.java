package study.datajpa.repository;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {

    //오픈 프로젝션
    @Value("#{target.username + ' ' + target.age}")
    String getUsername();
}
