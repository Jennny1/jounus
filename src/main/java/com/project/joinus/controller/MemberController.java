package com.project.joinus.controller;

import com.project.joinus.entity.MemberEntity;
import com.project.joinus.error.ResponseError;
import com.project.joinus.exception.EmailExistException;
import com.project.joinus.exception.EmailNotFountException;
import com.project.joinus.exception.PasswordNotFountException;
import com.project.joinus.model.Member;
import com.project.joinus.model.MemberDelete;
import com.project.joinus.model.MemberInput;
import com.project.joinus.model.MemberPasswordInput;
import com.project.joinus.model.MemberUpdateInput;
import com.project.joinus.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
  private final MemberRepository memberRepository;

  /*
  회원등록
  * 회원 아이디, 패스워드, 관심사를 입력 받는다.
  * 첫 가입 축하 포인트 100점을 부여한다.
  */

  @PostMapping("/add")
  public ResponseEntity<List<ResponseError>> addMember(@RequestBody @Valid MemberInput memberInput, Errors errors){
    List<ResponseError> responseErrorList = new ArrayList<>();

    if (errors.hasErrors()) {
      errors.getAllErrors().forEach((e) -> {
        responseErrorList.add(ResponseError.of((FieldError) e));
      });

      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);

    }


    // 이메일 검색 (동일 이메일 가입 불가)
    boolean existEmail = memberRepository.existsByEmail(memberInput.getEmail());

    if (existEmail) {
      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);
    }


    MemberEntity memberEntity = MemberEntity.builder()
        .userName(memberInput.getUserName())
        .password(memberInput.getPassword())
        .email(memberInput.getEmail())
        .favorit(memberInput.getFavorit())
        .point(100)
        .regDate(LocalDateTime.now())
        .build();

    memberRepository.save(memberEntity);

    return ResponseEntity.ok().build();

  }


  /*
  회원수정
  * 정보 수정은 email이 동일할때
  유저이름, 관심사 수정이 가능하다.
   */

  @PatchMapping("/update")
  public ResponseEntity<?> updateMember(@RequestBody @Valid MemberUpdateInput memberUpdateInput, Errors errors) {

    List<ResponseError> responseErrorList = new ArrayList<>();
    if (errors.hasErrors()) {
      errors.getAllErrors().forEach((e) -> {
        responseErrorList.add(ResponseError.of((FieldError) e));
      });

      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);

    }
    
    // 이메일 검색
    MemberInput memberInput = memberRepository.findByEmail(memberUpdateInput.getEmail())
        .orElseThrow(() -> new EmailNotFountException("검색한 이메일 정보가 없습니다."));

    String userName, favoritPick;

    System.out.println("userName : " + memberUpdateInput.getUserName());
    System.out.println("favofit : " + memberUpdateInput.getFavorit());


    if (memberUpdateInput.getUserName().equals("")) {
      userName = memberInput.getUserName();
    }
    userName = memberUpdateInput.getUserName();


    if (memberUpdateInput.getFavorit().equals("")) {
      favoritPick = memberInput.getFavorit();
    }
    favoritPick = memberUpdateInput.getFavorit();



    MemberEntity memberEntity = MemberEntity.builder()
            .userName(userName)
            .favorit(favoritPick)
            .updateDate(LocalDateTime.now())
            .build();

    memberRepository.save(memberEntity);

    return ResponseEntity.ok().build();

  }



  /*
  비밀번호 변경
  비밀번호 수정은 기존에 입력한 비밀번호와 동일할 때 가능하다.
   */

  @PatchMapping("/update/password")
  public ResponseEntity<?> updatePassword (@RequestBody @Valid MemberPasswordInput memberPasswordInput, Errors errors) {

    List<ResponseError> responseErrorList = new ArrayList<>();
    if (errors.hasErrors()) {

      errors.getAllErrors().forEach((e) -> {
        responseErrorList.add(ResponseError.of((FieldError) e));
      });

      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);

    }

    // 기존 비밀번호 검색
    MemberInput memberInput = memberRepository.findByUserNameAndPassword(memberPasswordInput.getUserName(), memberPasswordInput.getPassword());


    // 비밀번호 일치
    MemberEntity memberEntity = MemberEntity.builder()
            .password(memberPasswordInput.getNewPassword())
            .updateDate(LocalDateTime.now())
            .build();
    memberRepository.save(memberEntity);
    return ResponseEntity.ok().build();

  }


  /*
   회원 탈퇴시 회원 이메일이 동일할 때, 탈퇴 flag를 true로 변경한다.
   * 회원 탈퇴 시 포인트는 0으로 초기화한다.
   */

  @PatchMapping("/deletd")
  public ResponseEntity<List<ResponseError>> deleteMember(@RequestBody @Valid MemberDelete memberDelete, Errors errors){

    List<ResponseError> responseErrorList = new ArrayList<>();
    if (errors.hasErrors()) {
      errors.getAllErrors().forEach((e) -> {
        responseErrorList.add(ResponseError.of((FieldError) e));
      });

      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);

    }

    // 이메일 검색
    MemberInput memberInput = memberRepository.findByEmail(memberDelete.getEmail())
            .orElseThrow(() -> new EmailNotFountException("검색한 이메일 정보가 없습니다."));



    // 이메일 일치
    if (memberDelete.isQuit() != false) {
      return new ResponseEntity<>(responseErrorList, HttpStatus.BAD_REQUEST);

    }

    MemberEntity memberEntity = MemberEntity.builder()
            .isQuit(true)
            .point(0)
            .updateDate(LocalDateTime.now()).build();


    memberRepository.save(memberEntity);
    return ResponseEntity.ok().build();

  }



  // ExceptionHandler
  @ExceptionHandler (value = {EmailNotFountException.class, PasswordNotFountException.class})
  public ResponseEntity<?> ExceptionHandler(RuntimeException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }




}
