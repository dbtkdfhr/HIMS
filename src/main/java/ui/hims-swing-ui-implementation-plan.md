# HIMS Swing UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** HIMS 재고 및 입출고 관리 시나리오를 서비스 연결 없이 Swing UI에서 시연 가능하게 만든다.

**Architecture:** `Main.java`는 Swing 앱만 시작하고, `ui.HimsApplication`이 JFrame과 화면 전환을 담당한다. `ui.data.MockDataStore`가 기존 DTO 기반 더미 상태를 보관하며, 역할별 패널은 이 데이터를 읽고 갱신한다.

**Tech Stack:** Java, Swing, Gradle Java Plugin

---

## 파일 구조

- Modify: `src/main/java/Main.java`
- Create: `src/main/java/ui/HimsApplication.java`
- Create: `src/main/java/ui/LoginPanel.java`
- Create: `src/main/java/ui/DashboardPanel.java`
- Create: `src/main/java/ui/StoreManagerPanel.java`
- Create: `src/main/java/ui/VendorManagerPanel.java`
- Create: `src/main/java/ui/BranchManagerPanel.java`
- Create: `src/main/java/ui/SystemManagerPanel.java`
- Create: `src/main/java/ui/common/UiConstants.java`
- Create: `src/main/java/ui/common/UiTableFactory.java`
- Create: `src/main/java/ui/data/MockDataStore.java`

## 설계 반영 기준

이 계획은 `hims-swing-ui-design.md`의 최신 역할별 메뉴를 기준으로 한다.

- UI 전용 모델 클래스는 새로 만들지 않는다.
- 재고, 발주, 입고, 직원, 매장, 상품 데이터는 기존 DTO를 사용한다.
- 기존 DTO에 생성자나 보조 접근자가 부족하면 Lombok 어노테이션을 기존 DTO에만 추가한다.
- 입점매장 담당자 화면에는 `내 발주 요청 목록` 메뉴를 만들지 않는다.
- 지점 관리자 화면에는 `입점매장 등록 폼`과 `상품 등록 폼`만 등록 화면으로 둔다.
- 지점 관리자 화면에는 별도 `재고 수정 폼`, `지점 등록 폼`, `브랜드 등록 폼`, `카테고리 등록 폼`, `발주처 등록 폼`을 만들지 않는다.
- 서비스, DAO, DB는 연결하지 않는다.
- 테스트 코드는 작성하지 않는다.

## Task 1: 기존 DTO 확인과 더미 데이터 저장소 작성

**Files:**
- Create: `src/main/java/ui/data/MockDataStore.java`
- Modify only if needed: `src/main/java/employee/EmployeeDTO.java`
- Modify only if needed: `src/main/java/store/StoreDTO.java`
- Modify only if needed: `src/main/java/product/ProductDTO.java`
- Modify only if needed: `src/main/java/inventory/InventoryDTO.java`
- Modify only if needed: `src/main/java/order/request/OrderRequestDTO.java`
- Modify only if needed: `src/main/java/store/receipt/StoreReceiptDTO.java`

- [ ] **Step 1: 사용할 기존 DTO 확정**

다음 DTO를 UI 데이터 모델로 사용한다.

- `employee.EmployeeDTO`: 로그인 사용자, 직원 목록, 직원 계정 생성/정지
- `store.StoreDTO`: 입점매장 조회와 입점매장 등록
- `product.ProductDTO`: 상품 등록과 상품 선택
- `inventory.InventoryDTO`: 매장 재고 조회, 안전재고 부족 조회, 판매 처리
- `order.request.OrderRequestDTO`: 발주 요청, 승인, 반려, 외부 전송, 출고 상태
- `store.receipt.StoreReceiptDTO`: 입고 확인, 입고 차이, 입고 반려, 입고 이력

- [ ] **Step 2: DTO 보강 필요 여부 확인**

기존 DTO 대부분은 Lombok getter/setter 또는 `@Data`가 있다. 구현 중 생성자 부족으로 코드가 과도하게 길어질 때만 기존 DTO에 Lombok `@NoArgsConstructor`, `@AllArgsConstructor`를 추가한다.

- [ ] **Step 3: MockDataStore 작성**

초기 직원, 매장, 상품, 재고, 발주, 입고 데이터를 기존 DTO 리스트로 생성한다.

마스터 등록 결과는 새 모델을 만들지 않고 `Map<String, List<String[]>>` 또는 `List<String[]>`로 관리한다.

- [ ] **Step 4: 상태 변경 메서드 작성**

다음 메서드를 제공한다.

- `createOrderRequest`
- `approveOrder`
- `rejectOrder`
- `sendOrderToVendor`
- `shipOrder`
- `confirmReceipt`
- `markReceiptDifference`
- `rejectReceipt`
- `processSale`
- `createEmployee`
- `deactivateEmployee`
- `addMasterRecord`

- [ ] **Step 5: 표시용 조회 헬퍼 작성**

발주/입고 화면에서 DTO에 없는 매장명, 상품명, 브랜드명 같은 표시값은 다음 방식으로 얻는다.

- `findStoreName(long storeId)`
- `findProductName(long productId)`
- `findInventory(long storeId, long productId)`
- `findOrdersByStatus(String status)`
- `findReceiptsByStore(long storeId)`

- [ ] **Step 6: 컴파일 확인**

Run: `./gradlew compileJava`

Expected: `BUILD SUCCESSFUL`

## Task 2: 공통 UI 유틸과 앱 프레임 작성

**Files:**
- Create: `src/main/java/ui/common/UiConstants.java`
- Create: `src/main/java/ui/common/UiTableFactory.java`
- Create: `src/main/java/ui/HimsApplication.java`
- Modify: `src/main/java/Main.java`

- [ ] **Step 1: 공통 스타일 상수 작성**

폰트, 여백, 기본 색상, 위험 색상, 성공 색상을 정의한다.

- [ ] **Step 2: JTable 생성 유틸 작성**

테이블 생성, 스크롤 패널 래핑, 컬럼 너비 조정 기능을 제공한다.

- [ ] **Step 3: HimsApplication 작성**

JFrame을 만들고 로그인 화면과 대시보드 화면 전환 메서드를 제공한다.

- [ ] **Step 4: Main.java 연결**

`SwingUtilities.invokeLater`로 `HimsApplication`을 시작한다.

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew compileJava`

Expected: `BUILD SUCCESSFUL`

## Task 3: 로그인 화면 작성

**Files:**
- Create: `src/main/java/ui/LoginPanel.java`
- Modify: `src/main/java/ui/HimsApplication.java`

- [ ] **Step 1: 로그인 폼 작성**

로그인 ID, 비밀번호, 역할 선택 콤보박스, 로그인 버튼을 배치한다.

- [ ] **Step 2: 입력 검증 작성**

로그인 ID가 비어 있으면 안내 메시지를 표시한다. 비밀번호는 서비스 연결 전까지 값 존재 여부만 확인한다.

- [ ] **Step 3: 역할별 사용자 선택 작성**

선택한 역할에 맞는 더미 사용자를 `MockDataStore`에서 가져온다.

- [ ] **Step 4: 대시보드 이동 연결**

로그인 성공 시 `HimsApplication.showDashboard`를 호출한다.

- [ ] **Step 5: 수동 실행 확인**

Run: IDE에서 `Main.main` 실행

Expected: 로그인 화면 표시 후 역할 선택 로그인 가능

## Task 4: 대시보드 컨테이너 작성

**Files:**
- Create: `src/main/java/ui/DashboardPanel.java`
- Modify: `src/main/java/ui/HimsApplication.java`

- [ ] **Step 1: 공통 레이아웃 작성**

상단 헤더, 좌측 메뉴, 중앙 콘텐츠, 하단 로그 영역을 구성한다.

- [ ] **Step 2: 역할별 메뉴 구성 작성**

선택된 사용자 역할에 따라 메뉴 버튼을 다르게 표시한다.

입점매장 담당자 메뉴는 다음만 표시한다.

- 내 매장 재고 조회
- 안전재고 부족 상품 조회
- 발주 요청 생성
- 입고 검수 대상 조회
- 정상 입고 처리
- 입고 수량 차이 처리
- 입고 반려 처리
- 입고 이력 조회
- 판매 처리

발주처 담당자 메뉴는 다음만 표시한다.

- 발주 요청 목록 조회
- 발주 요청 상세 확인
- 발주 승인
- 발주 반려
- 승인 발주 외부 전송
- 외부 출고 처리
- 발주 상태별 필터링
- 발주 승인/반려 이력 조회

지점 관리자 메뉴는 다음만 표시한다.

- 전체 재고 현황 조회
- 지점별 재고 조회
- 브랜드별 재고 조회
- 카테고리별 재고 조회
- 입점매장 정보 조회
- 입점매장 등록 폼
- 상품 등록 폼

시스템 관리자 메뉴는 다음만 표시한다.

- 직원 계정 목록
- 직원 계정 생성 폼
- 직원 권한 변경 폼
- 직원 계정 정지 처리

- [ ] **Step 3: 콘텐츠 패널 교체 작성**

메뉴 버튼 클릭 시 중앙 영역에 역할별 패널 또는 안내 패널을 표시한다.

- [ ] **Step 4: 로그아웃 연결**

로그아웃 버튼 클릭 시 로그인 화면으로 돌아간다.

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew compileJava`

Expected: `BUILD SUCCESSFUL`

## Task 5: 입점매장 담당자 화면 작성

**Files:**
- Create: `src/main/java/ui/StoreManagerPanel.java`

- [ ] **Step 1: 재고 조회 테이블 작성**

매장 재고를 표시하고 안전재고 이하 행을 강조한다.

- [ ] **Step 2: 발주 요청 생성 폼 작성**

상품 선택, 요청수량, 요청사유 입력 후 `REQUESTED` 발주를 생성한다.

- [ ] **Step 3: 입고 검수 화면 작성**

`SHIPPED` 상태 발주를 대상으로 정상 입고, 수량 차이, 반려 처리를 제공한다.

- [ ] **Step 4: 입고 이력 조회 화면 작성**

더미 입고 이력을 테이블로 표시한다.

- [ ] **Step 5: 판매 처리 화면 작성**

상품과 판매수량을 입력받아 재고를 차감한다. 재고 부족이면 메시지를 표시한다.

- [ ] **Step 6: 수동 실행 확인**

Run: IDE에서 `Main.main` 실행

Expected: 매장 담당자 역할에서 재고 조회, 안전재고 부족 조회, 발주 요청, 입고 확인, 입고 이력 조회, 판매 처리 흐름 진행 가능

## Task 6: 발주처 담당자 화면 작성

**Files:**
- Create: `src/main/java/ui/VendorManagerPanel.java`

- [ ] **Step 1: 발주 요청 목록 작성**

상태 필터와 발주 테이블을 표시한다.

- [ ] **Step 2: 승인 처리 작성**

`REQUESTED` 발주를 `APPROVED`로 변경하고 승인수량을 저장한다.

- [ ] **Step 3: 반려 처리 작성**

`REQUESTED` 발주를 `REJECTED_BY_VENDOR`로 변경하고 반려사유를 저장한다.

- [ ] **Step 4: 외부 전송 처리 작성**

`APPROVED` 발주를 `SENT_TO_VENDOR`로 변경한다.

- [ ] **Step 5: 출고 처리 작성**

`SENT_TO_VENDOR` 발주를 `SHIPPED`로 변경한다.

- [ ] **Step 6: 수동 실행 확인**

Run: IDE에서 `Main.main` 실행

Expected: 발주처 담당자 역할에서 승인, 반려, 외부 전송, 출고 상태 변경 가능

## Task 7: 지점 관리자 화면 작성

**Files:**
- Create: `src/main/java/ui/BranchManagerPanel.java`

- [ ] **Step 1: 전체 재고 조회 작성**

전체 매장의 재고를 지점, 브랜드, 카테고리, 매장, 상품명 기준으로 필터링한다.

- [ ] **Step 2: 입점매장 정보 조회 화면 작성**

더미 입점매장 정보를 테이블로 표시한다.

- [ ] **Step 3: 입점매장 등록 폼 작성**

매장명, 지점, 브랜드, 층 정보, 매장 위치, 운영 상태를 입력받고 더미 마스터 데이터에 등록한다.

- [ ] **Step 4: 상품 등록 폼 작성**

상품명, 브랜드, 카테고리, 판매가, 시즌구분, 상품상태를 입력받고 더미 마스터 데이터에 등록한다.

- [ ] **Step 5: 등록 결과 표시 작성**

등록된 더미 마스터 데이터를 테이블에 표시한다.

- [ ] **Step 6: 수동 실행 확인**

Run: IDE에서 `Main.main` 실행

Expected: 지점 관리자 역할에서 전체 재고 조회, 조건별 재고 조회, 입점매장 정보 조회, 입점매장 등록, 상품 등록 가능

## Task 8: 시스템 관리자 화면 작성

**Files:**
- Create: `src/main/java/ui/SystemManagerPanel.java`

- [ ] **Step 1: 직원 목록 작성**

직원 ID, 로그인 ID, 직원명, 역할, 사용여부를 테이블로 표시한다.

- [ ] **Step 2: 직원 생성 폼 작성**

로그인 ID, 직원명, 역할, 매장 ID를 입력받아 더미 직원 목록에 추가한다.

- [ ] **Step 3: 직원 정지 처리 작성**

선택한 직원의 사용여부를 `N`으로 변경한다.

- [ ] **Step 4: 수동 실행 확인**

Run: IDE에서 `Main.main` 실행

Expected: 시스템 관리자 역할에서 직원 생성과 정지 처리가 화면에 반영됨

## Task 9: 최종 연결과 수동 검증

**Files:**
- Modify: `src/main/java/ui/DashboardPanel.java`
- Modify: `src/main/java/ui/HimsApplication.java`
- Modify: `src/main/java/Main.java`

- [ ] **Step 1: 역할별 기본 진입 화면 연결**

로그인 직후 각 역할의 대표 화면이 표시되도록 한다.

- [ ] **Step 2: 발표 흐름 수동 검증**

다음 순서로 직접 실행한다.

1. 입점매장 담당자로 로그인
2. 안전재고 부족 상품 확인
3. 발주 요청 생성
4. 로그아웃
5. 발주처 담당자로 로그인
6. 발주 승인
7. 외부 전송
8. 출고 처리
9. 로그아웃
10. 입점매장 담당자로 로그인
11. 입고 검수 대상 확인
12. 정상 입고 처리
13. 판매 처리

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew compileJava`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 코드 리뷰 요청**

Superpowers `requesting-code-review` 절차로 구현 범위, 상태 전이, 서비스 미연결 조건 위반 여부를 검토한다.

## 승인 후 진행 조건

사용자가 이 문서를 승인하면 구현을 시작한다. 승인 전에는 Java 구현 파일을 생성하거나 수정하지 않는다.
