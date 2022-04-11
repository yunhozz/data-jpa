package study.datajpa.repository;

import study.datajpa.entity.Member;

import java.util.List;

/*
복잡한 쿼리를 관리하고 싶을 때 ex) query dsl, JDBC template
 */
public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
