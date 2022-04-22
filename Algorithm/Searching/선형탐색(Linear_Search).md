## 선형 탐색(Linear Search)
  
  

- 선형탐색은 자료를 탐색하는데 가장 무식하면서 이해하기 쉬운 알고리즘
- 일종의 브루트포스(Brute Force)의 종류, 따라서 특정 자료를 찾기위해서 모든 자료를 찾아봐야 할 수도 있음.
- 시간 복잡도 : O(N)

- 자료가 정렬되어있지 않거나, 어떠한 기준이나 정보 없이 하나씩 찾아야하는 경우 선형탐색을 사용하는 것이 효율적임.


```java
int [] arr = {10, 14, 19, 26, 27, 31, 33, 35, 42, 44};

int key = 33;
int index = -1;

// 순차적으로 값을 비교해보면서 같은 값이 있는 지 확인
for(int i = 0; i < arr.length; i++) {
	
	if(arr[i] == key) {
		index = i;
		break;
	} 
}

if(index < 0) Systme.out.println("Not Found");
else Systme.out.println("index :: " + index);
```
