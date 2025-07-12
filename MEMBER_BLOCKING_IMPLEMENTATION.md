# Member Blocking Feature Implementation

## Overview
This implementation adds member blocking functionality to the BeatBuddy backend, allowing users to block other members so their content (posts, comments, reviews) will not be displayed.

## Components Implemented

### 1. Core Entity & Repository
- **MemberBlock** entity with blocker/blocked relationship
- **MemberBlockRepository** with query methods
- Unique constraint prevents duplicate blocking relationships

### 2. Service Layer
**MemberService** enhanced with blocking methods:
- `blockMember(Long blockerId, Long blockedId)` - Block a member
- `unblockMember(Long blockerId, Long blockedId)` - Unblock a member  
- `getBlockedMemberIds(Long blockerId)` - Get list of blocked member IDs
- `isBlocked(Long blockerId, Long blockedId)` - Check if member is blocked

### 3. Query Repositories with Filtering
Enhanced repositories to filter out blocked members:

**VenueReviewQueryRepository:**
- `findAllReviewsSortedExcludingBlocked()`
- `findReviewsWithImagesSortedExcludingBlocked()`

**PostQueryRepository:**
- `findHotPostsWithin12HoursExcludingBlocked()`
- `findPostsByHashtagsExcludingBlocked()`

**CommentQueryRepository:**
- `findAllByPostIdExcludingBlocked()`
- `findAllByMemberIdAndPostIdInExcludingBlocked()`

**EventCommentQueryRepository:**
- `findAllByEventExcludingBlocked()`

### 4. REST API Endpoints
**Member blocking endpoints:**
- `POST /members/block` - Block a member
- `DELETE /members/block/{blockedMemberId}` - Unblock a member
- `GET /members/blocked` - Get blocked members list
- `GET /members/blocked/{targetMemberId}` - Check if member is blocked

### 5. Error Handling
New error codes in MemberErrorCode:
- `CANNOT_BLOCK_SELF` - Cannot block yourself
- `ALREADY_BLOCKED` - Member is already blocked
- `BLOCK_NOT_FOUND` - Block relationship not found

## Usage Examples

### Using in Service Layer
```java
@Service
public class ExampleService {
    private final MemberService memberService;
    private final VenueReviewQueryRepository venueReviewQueryRepository;
    
    public List<VenueReview> getReviewsForUser(Long venueId, Long memberId) {
        // Get blocked member IDs for current user
        List<Long> blockedMemberIds = memberService.getBlockedMemberIds(memberId);
        
        // Use new filtering method
        return venueReviewQueryRepository.findAllReviewsSortedExcludingBlocked(
            venueId, "latest", blockedMemberIds);
    }
}
```

### API Usage
```bash
# Block a member
curl -X POST /members/block \
  -H "Content-Type: application/json" \
  -d '{"blockedMemberId": 123}'

# Unblock a member  
curl -X DELETE /members/block/123

# Get blocked members list
curl -X GET /members/blocked

# Check if member is blocked
curl -X GET /members/blocked/123
```

## Integration Guide for Existing Services

### Step 1: Add Blocking Support to Service
```java
@Service 
public class YourService {
    private final MemberService memberService; // Add this dependency
    
    public List<YourEntity> getContent(Long userId) {
        // Get blocked member IDs
        List<Long> blockedMemberIds = memberService.getBlockedMemberIds(userId);
        
        // Use your new filtering query method
        return yourQueryRepository.findContentExcludingBlocked(blockedMemberIds);
    }
}
```

### Step 2: Update Repository Interface
```java
public interface YourQueryRepository {
    // Add new method with blocked member filtering
    List<YourEntity> findContentExcludingBlocked(List<Long> blockedMemberIds);
}
```

### Step 3: Implement QueryDSL Filtering
```java
@Repository
public class YourQueryRepositoryImpl implements YourQueryRepository {
    
    public List<YourEntity> findContentExcludingBlocked(List<Long> blockedMemberIds) {
        QYourEntity entity = QYourEntity.yourEntity;
        
        return queryFactory
            .selectFrom(entity)
            .where(
                // Your existing conditions,
                blockedMemberIds.isEmpty() ? null : entity.member.id.notIn(blockedMemberIds)
            )
            .fetch();
    }
}
```

## Database Schema
```sql
CREATE TABLE member_block (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_member_block (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES member(id),
    FOREIGN KEY (blocked_id) REFERENCES member(id)
);
```

## Implementation Notes

### Performance Considerations
- Empty blocked lists are handled efficiently (no additional WHERE clause)
- Queries use `notIn()` which is optimized by QueryDSL
- Unique constraints prevent duplicate blocking relationships

### Error Handling
- Self-blocking is prevented with validation
- Duplicate blocks throw appropriate exceptions
- Non-existent relationships are handled gracefully

### Extensibility
- Easy to add to new content types by following the established pattern
- Repository methods can be extended with additional filtering criteria
- Utility class provided for common blocking operations

## Testing
Comprehensive tests included:
- Unit tests for blocking service methods
- Repository filtering tests
- Error condition testing
- Integration scenarios

The implementation follows the existing codebase patterns and maintains consistency with current architecture.