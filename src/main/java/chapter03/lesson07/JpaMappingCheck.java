// [역할] 게시글 제목 길이 검증(200자 OK, 201자 예외)을 DB 없이 확인하는 실습.
package chapter03.lesson07;

// 제목 길이 검증(최대 200자)이 잘 동작하는지 확인하는 파일.
// DB를 쓰지 않고 자바 객체만 만들어 본다. 200자는 통과, 201자는 예외가 나야 정상이다.
public class JpaMappingCheck {

    public static void main(String[] args) {

//      JpaMember 만들기
        JpaMember member = new JpaMember("mapping01", "kim");

//      "a".repeat(200)으로 200자 제목 만들기
        String title200 = "a".repeat(200);

//      new JpaPost(...)로 정상 생성되는지 확인
        JpaPost valid = new JpaPost(member, title200, "body");

//      "a".repeat(201)로 201자 제목 만들기
        // 정상 생성된 게시글의 제목 길이를 출력한다 (200이 나와야 함)
        System.out.println("titleLength = " + valid.getTitle().length());

//      new JpaPost(...) 했을 때 IllegalArgumentException이 나는지 확인
        try {
            // 201자 제목은 JpaPost의 검증에 걸려 예외가 발생한다
            new JpaPost(member, "a".repeat(201), "body");
        } catch (IllegalArgumentException e) {
            // 예외 메시지 출력 ("title too long"이 나와야 함)
            System.out.println(e.getMessage());
        }
    }
}
