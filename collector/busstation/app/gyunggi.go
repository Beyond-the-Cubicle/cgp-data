package app

import (
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"regexp"
	"strings"

	"github.com/Beyond-the-Cubicle/cgp-data/collector/busstation/store"
	"github.com/mitchellh/mapstructure"
	"github.com/twpayne/go-proj/v10"
)

type GyunggiOpenApiResponse struct {
	TBBMSSTATIONM []interface{}
}

type GyunggiOpenApiResponseHead struct {
	TotalCount int
	ResultCode OpenAPIResultCode
	ApiVersion string
}

type GyunggiOpenAPIBusStation struct {
	StationId               string  `mapstructure:"STTN_ID"`            // 정류장 ID
	StationName             string  `mapstructure:"STTN_NM"`            // 정류장 이름
	CoordinateY             float64 `mapstructure:"Y_CRDNT"`            // Y좌표
	CoordinateX             float64 `mapstructure:"X_CRDNT"`            // X좌표
	GpsCoordinateY          float64 `mapstructure:"GPS_X_CRDNT"`        // GPS Y좌표
	GpsCoordinateX          float64 `mapstructure:"GPS_Y_CRDNT"`        // GPS X좌표
	RinkId                  string  `mapstructure:"RINK_ID"`            // 링크아이디
	StationType             string  `mapstructure:"STTN_TYPE"`          // 정류장 유형
	TransferStationExtNo    string  `mapstructure:"TRANSIT_STTN_EXTNO"` // 환승 정류장 유무 ex. C, N
	MedianBusLaneYn         string  `mapstructure:"CNTR_CARTRK_YN"`     // 중앙차로 여부
	StationEnglishName      string  `mapstructure:"STTN_ENG_NM"`        // 정류장 영어 이름
	ArsId                   string  `mapstructure:"ARS_ID"`             // ARS ID
	InstitutionCode         string  `mapstructure:"INST_CD"`            // 기관 코드
	DataDisplayYn           string  `mapstructure:"DATA_EXPRS_EXTNO"`   // 데이터 표출 유무(Y/N)
	RegisteredBy            string  `mapstructure:"REGIST_ID"`          // 등록 주체 아이디
	RegisteredAt            string  `mapstructure:"REGIST_DE"`          // 등록 일시 YYYYMMDDHHmmss
	Memo                    string  `mapstructure:"RM"`                 // 비고
	SignPostType            string  `mapstructure:"SIGNPOST_TYPE"`      // 표지판 유형
	DongCode                string  `mapstructure:"ADMINIST_DONG_CD"`   // 행정동 코드
	RegionCode              string  `mapstructure:"VOLM_STATN_CD"`      // 권역 코드
	UseYn                   string  `mapstructure:"USE_DIV"`            // 사용구분(Y/N)
	StationChineseName      string  `mapstructure:"STTN_CHN_NM"`        // 정류장 중국어 이름
	StationJapaneseName     string  `mapstructure:"STTN_JPNLANG_NM"`    // 정류장 일본어 이름
	StationVietnamName      string  `mapstructure:"STTN_VIETNAM_NM"`    // 정류장 베트남어 이름
	DrtYn                   string  `mapstructure:"DRT_EXTNO"`          // DRT 유무
	StationTypeName         string  `mapstructure:"STATION_TP_NM"`      // 정류장 유형 이름(ex. 미지정, 시내)
	TransferStationTypeName string  `mapstructure:"CHNG_STATION_YN_NM"` // 환승유무를 나타내는 문자열(ex. 일반, 환승)
	SignPostTypeName        string  `mapstructure:"MARK_TYPE_NM"`       // 표지판 유형 이름(ex. 표지판 없음)
}

func (gyunggiOpenAPIBusStation *GyunggiOpenAPIBusStation) ToBusStation() store.StandardBusStation {
	regex := regexp.MustCompile("[0-9]+")
	arsIdCandidates := regex.FindAllString(gyunggiOpenAPIBusStation.ArsId, -1)
	arsId := gyunggiOpenAPIBusStation.ArsId
	if arsIdCandidates != nil {
		arsId = arsIdCandidates[0]
	}

	return store.StandardBusStation{
		StationName: gyunggiOpenAPIBusStation.StationName,
		StationId:   gyunggiOpenAPIBusStation.StationId,
		ArsId:       arsId,
		Latitude:    gyunggiOpenAPIBusStation.CoordinateY,
		Longitude:   gyunggiOpenAPIBusStation.CoordinateX,
	}
}

func (app *app) CollectGyunggiBusStations(docType DocType) ([]GyunggiOpenAPIBusStation, error) {
	var apiError OpenAPIError
	pageIndex := 1
	pageSize := 1000
	var gyunggiOpenAPIBusStations []GyunggiOpenAPIBusStation

	responseForCount, apiError, _ := requestGyunggiBusStations(app.gyunggiApiKey, docType, 1, 1)
	if (apiError != OpenAPIError{}) {
		errorMessage := fmt.Sprintf("[에러 응답 수신] URL: %s, code: %s, message: %s\n", apiError.Url, apiError.Result.Code, apiError.Result.Message)
		return nil, errors.New(errorMessage)
	}
	headForTotalCount := extractGyunggiOpenApiResponseHead(responseForCount)
	fmt.Printf("경기도 수집 대상 버스정류장 개수: %d\n", headForTotalCount.TotalCount)

	for {
		response, apiError, url := requestGyunggiBusStations(app.gyunggiApiKey, docType, pageIndex, pageSize)
		if (apiError != OpenAPIError{}) {
			errorMessage := fmt.Sprintf("[에러 응답 수신] URL: %s, code: %s, message: %s\n", apiError.Url, apiError.Result.Code, apiError.Result.Message)
			return nil, errors.New(errorMessage)
		}

		head := extractGyunggiOpenApiResponseHead(responseForCount)
		if head.ResultCode.Code != "INFO-000" {
			fmt.Printf("[정상 처리되지 않은 응답코드 수신] URL: %s, ResultCode: %#v\n", url, head.ResultCode)
			continue
		}

		// 정류장 정보 수집리스트에 추가
		gyunggiOpenAPIBusStations = append(gyunggiOpenAPIBusStations, extractGyunggiOpenAPIBusStation(response)...)

		pageIndex += 1
		// 페이지 크기와 인덱스를 곱한 값이 전체 데이터 수보다 커지면 중단
		if pageIndex*pageSize > headForTotalCount.TotalCount {
			break
		}
	}

	fmt.Printf("경기도 수집된 버스정류장 개수: %d\n", len(gyunggiOpenAPIBusStations))

	return gyunggiOpenAPIBusStations, nil
}

func (app *app) ConvertGyunggiBusStationsToStandard(gyunggiOpenApiBusStations []GyunggiOpenAPIBusStation) ([]store.StandardBusStation, error) {
	var busStations []store.StandardBusStation
	correctedGyunggiBusStations := correctGyunggiBusStations(gyunggiOpenApiBusStations)
	for _, gyunggiOpenApiBusStation := range correctedGyunggiBusStations {
		busStations = append(busStations, gyunggiOpenApiBusStation.ToBusStation())
	}

	fmt.Printf("경기도 필터링 후 버스정류장 개수: %d\n", len(busStations))
	return busStations, nil
}

func correctGyunggiBusStations(gyunggiOpenApiBusStations []GyunggiOpenAPIBusStation) []GyunggiOpenAPIBusStation {
	var correctedGyunggiBusStations []GyunggiOpenAPIBusStation
	for _, gyunggiOpenApiBusStation := range gyunggiOpenApiBusStations {
		// 경기도 정류장 데이터에서 ArsId에 "서울"이 들어가는 케이스는 서울시에서 제공하는 정류장 데이터와 중복되므로 제거
		if strings.Contains(gyunggiOpenApiBusStation.ArsId, "서울") {
			continue
		}
		// 정류장 이름이 없는 데이터 제외
		if gyunggiOpenApiBusStation.StationName == "" {
			continue
		}
		// ArsId가 없는 데이터 제외
		if gyunggiOpenApiBusStation.ArsId == "" {
			continue
		}
		// bessel x, y 좌표 WGS84 - 위경도로 변환
		latitude, longitude, err := bessel1884TMToWGS84(gyunggiOpenApiBusStation.CoordinateX, gyunggiOpenApiBusStation.CoordinateY)
		if err != nil {
			panic("경기도 좌표 변환(bessel -> WGS84) 실패 - " + err.Error())
		}
		gyunggiOpenApiBusStation.CoordinateX = longitude
		gyunggiOpenApiBusStation.CoordinateY = latitude

		correctedGyunggiBusStations = append(correctedGyunggiBusStations, gyunggiOpenApiBusStation)
	}
	return correctedGyunggiBusStations
}

func bessel1884TMToWGS84(x, y float64) (float64, float64, error) {
	// EPSG:4326 -> WGS84 경위도
	// EPSG:5174 -> 오래된 지리원 표준 중 보정된 중부원점(Bessel) - KLIS에서 중부지역에 사용중
	pj, err := proj.NewCRSToCRS("EPSG:5174", "EPSG:4326", nil)
	if err != nil {
		return 0, 0, err
	}
	inputCoordinate := proj.NewCoord(x, y, 0, 0)
	convertedCoordinate, err := pj.Forward(inputCoordinate)
	if err != nil {
		return 0, 0, err
	}

	return convertedCoordinate.X(), convertedCoordinate.Y(), nil
}

func (app *app) InsertGyunggiBusStations(gyunggiOpenApiBusStations []GyunggiOpenAPIBusStation) error {
	for _, gyunggiOpenApiBusStation := range gyunggiOpenApiBusStations {
		err := app.gyunggiStore.CreateBusStations(
			gyunggiOpenApiBusStation.StationId,
			gyunggiOpenApiBusStation.StationName,
			gyunggiOpenApiBusStation.CoordinateX,
			gyunggiOpenApiBusStation.CoordinateY,
			gyunggiOpenApiBusStation.GpsCoordinateX,
			gyunggiOpenApiBusStation.GpsCoordinateY,
			gyunggiOpenApiBusStation.RinkId,
			gyunggiOpenApiBusStation.StationType,
			gyunggiOpenApiBusStation.TransferStationExtNo,
			gyunggiOpenApiBusStation.MedianBusLaneYn,
			gyunggiOpenApiBusStation.StationEnglishName,
			gyunggiOpenApiBusStation.ArsId,
			gyunggiOpenApiBusStation.InstitutionCode,
			gyunggiOpenApiBusStation.DataDisplayYn,
			gyunggiOpenApiBusStation.RegisteredBy,
			gyunggiOpenApiBusStation.RegisteredAt,
			gyunggiOpenApiBusStation.Memo,
			gyunggiOpenApiBusStation.SignPostType,
			gyunggiOpenApiBusStation.DongCode,
			gyunggiOpenApiBusStation.RegionCode,
			gyunggiOpenApiBusStation.UseYn,
			gyunggiOpenApiBusStation.StationChineseName,
			gyunggiOpenApiBusStation.StationJapaneseName,
			gyunggiOpenApiBusStation.StationVietnamName,
			gyunggiOpenApiBusStation.DrtYn,
			gyunggiOpenApiBusStation.StationTypeName,
			gyunggiOpenApiBusStation.TransferStationTypeName,
			gyunggiOpenApiBusStation.SignPostTypeName,
		)
		if err != nil {
			return err
		}
	}
	return nil
}

func requestGyunggiBusStations(apiKey string, docType DocType, pageIndex int, pageSize int) (GyunggiOpenApiResponse, OpenAPIError, string) {
	var apiError OpenAPIError
	var openAPIFailResponse OpenAPIFailResponse
	var rawOpenApiResponse GyunggiOpenApiResponse

	url := fmt.Sprintf("https://openapi.gg.go.kr/TBBMSSTATIONM?KEY=%s&Type=%s&pIndex=%d&pSize=%d", apiKey, docType, pageIndex, pageSize)
	response, err := http.Get(url)
	if err != nil {
		panic(err)
	}

	defer response.Body.Close()

	jsonByte, err := io.ReadAll(response.Body)
	if err != nil {
		panic(err)
	}

	// 정상 응답이 아닌 케이스
	json.Unmarshal(jsonByte, &openAPIFailResponse)
	if openAPIFailResponse.Result.Code != "" {
		apiError = OpenAPIError{
			Url:    url,
			Result: openAPIFailResponse.Result,
		}
		return GyunggiOpenApiResponse{}, apiError, url
	}

	json.Unmarshal(jsonByte, &rawOpenApiResponse)
	return rawOpenApiResponse, apiError, url
}

func extractGyunggiOpenApiResponseHead(response GyunggiOpenApiResponse) GyunggiOpenApiResponseHead {
	rawOpenApiHeadMapList := response.TBBMSSTATIONM[0].(map[string]interface{})["head"].([]interface{})
	totalCount := rawOpenApiHeadMapList[0].(map[string]interface{})["list_total_count"]

	openApiResultcode := rawOpenApiHeadMapList[1].(map[string]interface{})["RESULT"].(map[string]interface{})
	resultCode := OpenAPIResultCode{
		Code:    openApiResultcode["CODE"].(string),
		Message: openApiResultcode["MESSAGE"].(string),
	}

	apiVersion := rawOpenApiHeadMapList[2].(map[string]interface{})["api_version"].(string)

	head := GyunggiOpenApiResponseHead{
		TotalCount: int(totalCount.(float64)),
		ResultCode: resultCode,
		ApiVersion: apiVersion,
	}
	return head
}

func extractGyunggiOpenAPIBusStation(response GyunggiOpenApiResponse) []GyunggiOpenAPIBusStation {
	var gyunggiOpenAPIBusStations []GyunggiOpenAPIBusStation

	rawOpenApiRowMapList := response.TBBMSSTATIONM[1].(map[string]interface{})["row"].([]interface{})
	err := mapstructure.Decode(rawOpenApiRowMapList, &gyunggiOpenAPIBusStations)

	if err != nil {
		panic(err)
	}
	return gyunggiOpenAPIBusStations
}
