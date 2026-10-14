// [역할] 게시글 상태 값(DRAFT 초안 / PUBLISHED 공개 / ARCHIVED 보관)을 정의한 enum.
package chapter03.lesson07;

// 게시글의 상태를 나타내는 enum(정해진 값 중 하나만 고를 수 있는 타입).
// 오타가 나기 쉬운 문자열("draft", "Draft"...) 대신 이 세 값만 쓰게 해서 실수를 막는다.
public enum PostStatus {
    DRAFT,      // 초안: 작성 중. 아직 공개 전이라 댓글을 달 수 없다
    PUBLISHED,  // 공개: 누구나 볼 수 있고 댓글을 달 수 있다
    ARCHIVED    // 보관: 더 이상 쓰지 않는 글
}
